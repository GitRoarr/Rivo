const mongoose = require("mongoose")
const bcrypt = require("bcryptjs")

const userSchema = mongoose.Schema({
    _id: {
        type: String,
        required: true,
    },
    email: {
        type: String,
        required: true,
        unique: true,
    },
    password: {
        type: String,
        required: true,
    },
    name: {
        type: String,
        required: true,
    },
    fullName: {
        type: String,
        required: true,
    },
    userType: {
        type: String,
        required: true,
        enum: ["ADMIN", "ARTIST", "LISTENER", "GUEST"],
        default: "LISTENER",
    },
    bio: {
        type: String,
        default: "",
    },
    profileImageUrl: {
        type: String,
        default: "",
    },
    coverImageUrl: {
        type: String,
        default: "",
    },
    location: {
        type: String,
        default: "",
    },
    website: {
        type: String,
        default: "",
    },
    socialLinks: {
        type: Map,
        of: String,
        default: {},
    },
    verificationStatus: {
        type: String,
        enum: ["UNVERIFIED", "PENDING", "VERIFIED", "REJECTED"],
        default: "UNVERIFIED",
    },
    isApproved: {
        type: Boolean,
        default: false,
    },
    isSuspended: {
        type: Boolean,
        default: false,
    },
}, {
    timestamps: true,
},)

// Match user entered password to hashed password in database
userSchema.methods.matchPassword = async function (enteredPassword) {
    return await bcrypt.compare(enteredPassword, this.password)
}

// Encrypt password using bcrypt
userSchema.pre("save", async function (next) {
    if (!this.isModified("password")) {
        next()
    }

    const salt = await bcrypt.genSalt(10)
    this.password = await bcrypt.hash(this.password, salt)
})

const User = mongoose.model("User", userSchema)

module.exports = User