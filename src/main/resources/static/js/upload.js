const form = document.getElementById("uploadForm");
const button = document.getElementById("uploadButton");
const status = document.getElementById("status");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const title = document.getElementById("title").value.trim();
    const file = document.getElementById("file").files[0];

    if (!file || !title) {
        status.textContent = "Enter a title and select an audio file.";
        return;
    }

    button.disabled = true;
    try {
        const contentType = file.type || "application/octet-stream";
        status.textContent = "Preparing upload...";

        // Step 1: Generate a pre-signed S3 upload URL
        const urlResponse = await fetch("/api/songs/upload-url", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({fileName: file.name, contentType})
        });

        if (!urlResponse.ok) throw new Error("Could not generate upload URL.");
        const {uploadUrl, storageKey} = await urlResponse.json();

        // Step 2: Upload the audio file directly to S3
        status.textContent = "Uploading audio to S3...";
        const uploadResponse = await fetch(uploadUrl, {
            method: "PUT",
            headers: {"Content-Type": contentType},
            body: file
        });

        if (!uploadResponse.ok) {
            throw new Error("S3 upload failed: HTTP " + uploadResponse.status);
        }

        // Step 3: Save song metadata in MySQL
        status.textContent = "Saving song metadata...";
        const songResponse = await fetch("/api/songs", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                title: title,
                storageKey: storageKey,
                contentType: contentType,
                durationSeconds: null
            })
        });

        if (!songResponse.ok) {
            throw new Error("Audio uploaded, but saving song metadata failed.");
        }

        const song = await songResponse.json();
        status.textContent = "Song saved successfully! Song ID: " + song.id;
        form.reset();
    } catch (error) {
        status.textContent = error.message || "Upload failed.";
    } finally {
        button.disabled = false;
    }
});
