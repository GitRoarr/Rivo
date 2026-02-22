const mongoose = require("mongoose")

const featuredContentSchema = mongoose.Schema({
    id: {
        type: String,
        required: true,
        unique: true
    },
    type: {
        type: String,
        enum: ["BANNER", "SONG", "ARTIST", "PLAYLIST"],
        required: true
    },
    contentId: {
        type: String,
        required: false // Optional if it's just a general banner
    },
    title: {
        type: String,
        required: true
    },
    description: {
        type: String,
        default: ""
    },
    imageUrl: {
        type: String,
        default: ""
    },
    actionUrl: {
        type: String,
        default: ""
    },
    order: {
        type: Number,
        default: 0
    },
    isActive: {
        type: Boolean,
        default: true
    }
}, {
    timestamps: true
})

const FeaturedContent = mongoose.model("FeaturedContent", featuredContentSchema)

module.exports = FeaturedContent
