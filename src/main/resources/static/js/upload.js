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
        let storageKey;

        if (file.size > 5 * 1024 * 1024) {
            status.textContent = "Uploading large audio file in parts...";
            storageKey = await uploadMultipart(file);
        } else {
            const urlResponse = await fetch("/api/songs/upload-url", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({fileName: file.name, contentType})
            });
            if (!urlResponse.ok) throw new Error("Failed to generate upload URL: HTTP " + urlResponse.status);

            const {uploadUrl, storageKey: key} = await urlResponse.json();
            status.textContent = "Uploading audio to S3...";

            const uploadResponse = await fetch(uploadUrl, {
                method: "PUT", headers: {"Content-Type": contentType}, body: file
            });
            if (!uploadResponse.ok) throw new Error("S3 upload failed: HTTP " + uploadResponse.status);
            storageKey = key;
        }

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

async function uploadMultipart(file) {
    const partSize = 5 * 1024 * 1024;
    const initiateResponse = await fetch("/api/songs/multipart/initiate", {
        method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify({
            fileName: file.name, contentType: file.type || "application/octet-stream"
        })
    });
    if (!initiateResponse.ok) throw new Error("Failed to initiate multipart upload");
    const {storageKey, uploadId} = await initiateResponse.json();

    const parts = [];
    try {
        for (let start = 0, partNumber = 1; start < file.size; start += partSize, partNumber++) {
            const blob = file.slice(start, Math.min(start + partSize, file.size));
            const urlResponse = await fetch("/api/songs/multipart/upload-part", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({storageKey, uploadId, partNumber})
            });
            if (!urlResponse.ok) throw new Error(`Failed to get URL for part ${partNumber}`);
            const {uploadUrl} = await urlResponse.json();
            const uploadResponse = await fetch(uploadUrl, {
                method: "PUT", body: blob
            });
            if (!uploadResponse.ok) throw new Error(`Failed to upload part ${partNumber}`);
            const eTag = uploadResponse.headers.get("ETag");
            if (!eTag) throw new Error("ETag missing; check S3 bucket CORS ExposeHeaders");

            // store the mapping of part number to eTag
            parts.push({partNumber, eTag});
        }
        const completeResponse = await fetch("/api/songs/multipart/complete", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({storageKey, uploadId, parts})
        });
        if (!completeResponse.ok) throw new Error("Failed to complete multipart upload");
        return storageKey;
    } catch (error) {
        console.error("Multipart upload failed:", error);
        throw error;
    }
}

