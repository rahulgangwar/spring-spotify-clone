const form = document.getElementById("uploadForm");
const button = document.getElementById("uploadButton");
const status = document.getElementById("status");
const uploadPanel = document.getElementById("uploadPanel");
const toggleUpload = document.getElementById("toggleUpload");

toggleUpload.addEventListener("click", () => {
    uploadPanel.classList.toggle("hidden");
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const title = document.getElementById("title").value.trim();
    const file = document.getElementById("file").files[0];

    if (!title || !file) {
        status.textContent = "Enter a song title and select an audio file.";
        return;
    }

    button.disabled = true;
    status.textContent = "Preparing upload...";

    try {
        const contentType = file.type || "application/octet-stream";
        const urlResponse = await fetch("/api/songs/upload-url", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({fileName: file.name, contentType})
        });
        if (!urlResponse.ok) throw new Error("Failed to generate upload URL: HTTP " + urlResponse.status);

        const {uploadUrl, storageKey} = await urlResponse.json();
        status.textContent = "Uploading audio to S3...";

        const uploadResponse = await fetch(uploadUrl, {
            method: "PUT",
            headers: {"Content-Type": contentType},
            body: file
        });
        if (!uploadResponse.ok) throw new Error("S3 upload failed: HTTP " + uploadResponse.status);

        status.textContent = "Saving song metadata...";
        const songResponse = await fetch("/api/songs", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({title, storageKey, contentType, durationSeconds: null})
        });
        if (!songResponse.ok) throw new Error("Failed to save song metadata: HTTP " + songResponse.status);

        const song = await songResponse.json();
        status.textContent = "Uploaded successfully: " + song.title;
        form.reset();
        document.dispatchEvent(new Event("songs:refresh"));
    } catch (error) {
        status.textContent = error.message || "Upload failed.";
        console.error("Song upload failed:", error);
    } finally {
        button.disabled = false;
    }
});
