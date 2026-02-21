const mongoose = require("mongoose")

const notificationSchema = mongoose.Schema({
    user: {
        type: String,
        ref: "User",
        required: true
    },
    title: {
        type: String,
        required: true
    },
    message: {
        type: String,
        required: true
    },
    type: {
        type: String,
        enum: ["INFO", "SUCCESS", "WARNING", "ERROR", "FOLLOW", "UPLOAD", "APPROVAL", "REJECTION", "VERIFICATION", "NEW_MUSIC"],
        default: "INFO"
    },
    isRead: {
        type: Boolean,
        default: false
    },
    relatedId: {
        type: String,
        default: null  // For linking to related entity (musicId, userId, etc.)
    },
    data: {
        type: Object, // For extra info like artistId, musicId
        default: {}
    }
}, {
    timestamps: true
})

const Notification = mongoose.model("Notification", notificationSchema)

module.exports = Notification
