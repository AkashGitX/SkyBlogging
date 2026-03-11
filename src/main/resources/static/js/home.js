document.addEventListener('DOMContentLoaded', () => {
    const postsContainer = document.getElementById('posts-container');
    const searchInput = document.getElementById('search-input');
    let allPosts = [];

    async function loadPosts() {
        try {
            const response = await fetch('/api/posts');
            if (response.ok) {
                allPosts = await response.json();
                renderPosts(allPosts);
            }
        } catch (error) {
            postsContainer.innerHTML = '<p>Failed to load posts.</p>';
        }
    }

    function renderPosts(posts) {
        if (!postsContainer) return;
        
        if (posts.length === 0) {
            postsContainer.innerHTML = '<p>No posts found.</p>';
            return;
        }

        postsContainer.innerHTML = posts.map(post => `
            <div class="blog-card" onclick="window.location.href='/post/${post.id}'">
                <h3>${escapeHtml(post.title)}</h3>
                <p>${escapeHtml(post.content)}</p>
                <div class="blog-meta">
                    <span>By ${escapeHtml(post.author.name)}</span>
                    <span>${formatDate(post.createdAt)} · ${calculateReadTime(post.content)}</span>
                </div>
            </div>
        `).join('');
    }

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            const filtered = allPosts.filter(post => 
                post.title.toLowerCase().includes(term) || 
                post.content.toLowerCase().includes(term)
            );
            renderPosts(filtered);
        });
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

    loadPosts();
});