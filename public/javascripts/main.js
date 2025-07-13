const cache = {}

function showSpinner(show) {
    if (show) document.getElementById("content").innerHTML = ""
    document.getElementById("spinner").style.display = show ? "block" : "none"
}

function setActiveLink(path) {
    document.querySelectorAll(".nav-link").forEach(link => {
        if (link.getAttribute("href") === path) {
            link.classList.add("active")
        } else {
            link.classList.remove("active")
        }
    })
}

async function loadPage(url, push = true) {
    try {
        showSpinner(true)
        const res = await fetch(url, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        const html = await res.text()
        document.getElementById("content").innerHTML = html
        if (push) history.pushState({}, "", url)
        setActiveLink(url)
    } catch (e) {
        document.getElementById("content").innerHTML = "<p>Gagal memuat halaman.</p>"
    } finally {
        showSpinner(false)
    }
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".global-nav-link").forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault()
            const url = link.getAttribute("href")
            loadPage(url)
        })
    })

    window.addEventListener("popstate", () => {
        loadPage(location.pathname, false)
    })
})
