const mongoose = require("mongoose")

const categorySchema = mongoose.Schema({
    title: {
        type: String,
        required: [true, "Please add a category title"],
        unique: true
    },
    color: {
        type: String,
        default: "#FF6B6B"
    },
    icon: {
        type: String,
        default: "music_note"
    }
}, {
    timestamps: true
})

module.exports = mongoose.model("Category", categorySchema)
