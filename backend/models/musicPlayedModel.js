const mongoose = require("mongoose")

const musicPlayedSchema = mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        required: true
    },
    musicId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "Music",
        required: true
    },
    playedAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
})

// Index for efficient queries
musicPlayedSchema.index({ userId: 1 })
musicPlayedSchema.index({ musicId: 1 })
musicPlayedSchema.index({ playedAt: -1 })

const MusicPlayed = mongoose.model("MusicPlayed", musicPlayedSchema)

module.exports = MusicPlayed
