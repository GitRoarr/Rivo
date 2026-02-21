const mongoose = require("mongoose")

const followSchema = mongoose.Schema({
    followerId: {
        type: mongoose.Schema.Types.ObjectId,
        required: true,
        ref: "User",
    },
    followingId: {
        type: mongoose.Schema.Types.ObjectId,
        required: true,
        ref: "User",
    },
}, {
    timestamps: true,
},)

followSchema.index({ followerId: 1, followingId: 1 }, { unique: true })

module.exports = mongoose.model("Follow", followSchema)