const mongoose = require("mongoose")

const musicSchema = mongoose.Schema({
    title: {
        type: String,
        required: [true, "Please add a music title"]
    },
    artist: {
        type: String,
        ref: "User",
        required: true
    },
    artistName: {
        type: String,
        required: true
    },
    genre: {
        type: String,
        required: [true, "Please add a genre"]
    },
    album: {
        type: String,
        default: "Single"
    },
    url: {
        type: String,
        required: [true, "Please add the music file URL"]
    },
    coverImageUrl: {
        type: String,
        default: ""
    },
    duration: {
        type: Number,
        default: 0
    },
    plays: {
        type: Number,
        default: 0
    },
    isApproved: {
        type: Boolean,
        default: false
    }
}, {
    timestamps: true
})

const Music = mongoose.model("Music", musicSchema)

module.exports = Music
