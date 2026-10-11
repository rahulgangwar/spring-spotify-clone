const songList = document.getElementById("songList");
const songCount = document.getElementById("songCount");

async function loadSongs() {
    try {
        const response = await fetch("/api/songs");
        if (!response.ok) throw new Error("Failed to load songs.");
        const songs = await response.json();
        songList.replaceChildren();
        songCount.textContent = songs.length + (songs.length === 1 ? " song" : " songs");

        if (songs.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");
            cell.colSpan = 5;
            cell.className = "empty-state";
            cell.textContent = "No songs yet. Upload your first song.";
            row.appendChild(cell);
            songList.appendChild(row);
            return;
        }

        songs.forEach((song, index) => {
            const row = document.createElement("tr");
            const numberCell = document.createElement("td");
            numberCell.className = "number-column";
            numberCell.textContent = index + 1;

            const titleCell = document.createElement("td");
            const title = document.createElement("div");
            title.className = "song-title";
            title.textContent = song.title;
            const subtitle = document.createElement("div");
            subtitle.className = "song-subtitle";
            subtitle.textContent = song.contentType || "Audio";
            titleCell.append(title, subtitle);

            const albumCell = document.createElement("td");
            albumCell.className = "album-column";
            albumCell.textContent = "—";

            const dateCell = document.createElement("td");
            dateCell.className = "date-column";
            dateCell.textContent = song.createdAt ? formatDate(song.createdAt) : "—";

            const durationCell = document.createElement("td");
            durationCell.className = "duration-column";
            durationCell.textContent = formatDuration(song.durationSeconds);

            row.append(numberCell, titleCell, albumCell, dateCell, durationCell);
            songList.appendChild(row);
        });
    } catch (error) {
        songList.replaceChildren();
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 5;
        cell.className = "empty-state";
        cell.textContent = error.message || "Could not load songs.";
        row.appendChild(cell);
        songList.appendChild(row);
    }
}

function formatDate(value) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "—";
    return date.toLocaleDateString("en-IN", {day: "numeric", month: "short", year: "numeric"});
}

function formatDuration(seconds) {
    if (seconds == null || !Number.isFinite(Number(seconds))) return "—";
    const total = Math.floor(Number(seconds));
    return Math.floor(total / 60) + ":" + String(total % 60).padStart(2, "0");
}

document.addEventListener("songs:refresh", loadSongs);
loadSongs();
