const express = require("express")
const dotenv = require("dotenv")
const mongoose = require("mongoose")
const cors = require("cors")
const { errorHandler } = require("./middleware/errorMiddleware")
const userRoutes = require("./routes/userRoutes")
const playlistRoutes = require("./routes/playlistRoutes")
const watchlistRoutes = require("./routes/watchlistRoutes")
const musicRoutes = require("./routes/musicRoutes")
const searchRoutes = require("./routes/searchRoutes")
const exploreRoutes = require("./routes/exploreRoutes")
const notificationRoutes = require("./routes/notificationRoutes")
const statsRoutes = require("./routes/statsRoutes")

dotenv.config()

const app = express()
const PORT = process.env.PORT || 5000

app.use(cors())
app.use(express.json())
app.use(express.urlencoded({ extended: false }))

app.use("/api/users", userRoutes)
app.use("/api/playlists", playlistRoutes)
app.use("/api/watchlists", watchlistRoutes)
app.use("/api/music", musicRoutes)
app.use("/api/search", searchRoutes)
app.use("/api/explore", exploreRoutes)
app.use("/api/notifications", notificationRoutes)
app.use("/api/stats", statsRoutes)
app.use("/api/featured", require("./routes/featuredContentRoutes"))

app.get("/", (req, res) => {
    res.send("Rivo Music API is running")
})

app.use(errorHandler)

mongoose.set('strictQuery', false)

const mongoUri = process.env.MONGODB_URI

if (!mongoUri) {
    console.error('Mongo URI is not defined!')
    process.exit(1)
}

mongoose
    .connect(mongoUri)
    .then(() => {
        app.listen(PORT, () => {
            console.log(`Server running on port ${PORT}`)
        })
        console.log("Connected to MongoDB")
    })
    .catch((err) => {
        console.error("Failed to connect to MongoDB", err)
        process.exit(1)
    })