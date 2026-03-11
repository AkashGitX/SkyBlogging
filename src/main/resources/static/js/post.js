document.addEventListener('DOMContentLoaded', () => {
    const pathParts = window.location.pathname.split('/');
    const postId = pathParts[pathParts.length - 1];
    
    const postContainer = document.getElementById('post-container');
    const commentsList = document.getElementById('comments-list');
    const user = getUser();

    async function loadPost() {
        try {
            const response = await fetch(`/api/posts/${postId}`);
            if (response.ok) {
                const post = await response.json();
                renderPost(post);
                loadComments();
            } else {
                postContainer.innerHTML = '<h1>Post not found</h1>';
            }
        } catch (error) {
            postContainer.innerHTML = '<h1>Error loading post</h1>';
        }
    }

    function renderPost(post) {
        const isAuthorOrAdmin = user && (user.id === post.author.id || user.role === 'ADMIN');
        
        postContainer.innerHTML = `
            <header class="post-header">
                <h1 class="post-title">${escapeHtml(post.title)}</h1>
                <div class="post-author-meta">
                    <span>By ${escapeHtml(post.author.name)}</span>
                    <span>${formatDate(post.createdAt)}</span>
                    <span>${calculateReadTime(post.content)}</span>
                    ${isAuthorOrAdmin ? `<button onclick="deletePost(${post.id})" class="btn-delete ml-auto">Delete Post</button>` : ''}
                </div>
            </header>
            <div class="post-body">${escapeHtml(post.content)}</div>
        `;
    }

    async function loadComments() {
        try {
            const response = await fetch(`/api/posts/${postId}/comments`);
            if (response.ok) {
                const comments = await response.json();
                renderComments(comments);
            }
        } catch (error) {
            commentsList.innerHTML = '<p>Error loading comments</p>';
        }
    }

    function renderComments(comments) {
        if (comments.length === 0) {
            commentsList.innerHTML = '<p>No comments yet. Be the first!</p>';
            return;
        }

        commentsList.innerHTML = comments.map(comment => {
            const canDelete = user && (user.id === comment.user.id || user.role === 'ADMIN');
            return `
                <div class="comment" id="comment-${comment.id}">
                    <div class="comment-meta">
                        ${escapeHtml(comment.user.name)} 
                        <span class="comment-date">${formatDate(comment.createdAt)}</span>
                        ${canDelete ? `<button onclick="deleteComment(${comment.id})" class="btn-delete" style="float:right">Delete</button>` : ''}
                    </div>
                    <div class="comment-content">${escapeHtml(comment.content)}</div>
                </div>
            `;
        }).join('');
    }

    // Setup comment form
    if (isAuthenticated()) {
        document.getElementById('comment-form-container').style.display = 'block';
        
        document.getElementById('submit-comment').addEventListener('click', async () => {
            const input = document.getElementById('comment-input');
            const content = input.value.trim();
            if (!content) return;

            try {
                const response = await fetchWithAuth(`/api/posts/${postId}/comments`, {
                    method: 'POST',
                    body: JSON.stringify({ content })
                });

                if (response.ok) {
                    input.value = '';
                    loadComments(); // reload to show new comment
                }
            } catch (error) {
                alert('Error posting comment');
            }
        });
    } else {
        document.getElementById('login-prompt').style.display = 'block';
    }

    // Expose delete functions to global scope for onclick handlers
    window.deletePost = async (id) => {
        if(!confirm('Are you sure you want to delete this post?')) return;
        try {
            const res = await fetchWithAuth(`/api/posts/${id}`, { method: 'DELETE' });
            if(res.ok) window.location.href = '/';
        } catch(e) { alert('Failed to delete post'); }
    }

    window.deleteComment = async (id) => {
        if(!confirm('Are you sure you want to delete this comment?')) return;
        try {
            const res = await fetchWithAuth(`/api/comments/${id}`, { method: 'DELETE' });
            if(res.ok) {
                document.getElementById(`comment-${id}`).remove();
            }
        } catch(e) { alert('Failed to delete comment'); }
    }

    // Helper to prevent XSS
    function escapeHtml(unsafe) {
        return (unsafe || '').toString()
             .replace(/&/g, "&amp;")
             .replace(/</g, "&lt;")
             .replace(/>/g, "&gt;")
             .replace(/"/g, "&quot;")
             .replace(/'/g, "&#039;");
    }

    loadPost();
});