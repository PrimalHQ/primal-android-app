package net.primal.android.profile.details.model

data class ProfileDetailsUi(
    val pubkey: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val coverUrl: String?,
    val avatarUrl: String?,
    val internetIdentifier: String?,
    val about: String?,
    val website: String?,
) { companion object }

fun ProfileDetailsUi.Companion.previewExample(): ProfileDetailsUi =
    ProfileDetailsUi(
        pubkey = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
        authorDisplayName = "He-Man",
        about = "Bacon ipsum dolor amet short loin chicken flank strip steak. Brisket ham tenderloin, tri-tip pork belly kevin ball tip shankle jerky tongue short ribs chislic. Fatback ham doner pork loin burgdoggen. Pork belly boudin beef, burgdoggen salami turducken short loin t-bone jowl ribeye shankle pork loin pancetta meatloaf flank. ",
        avatarUrl = "https://i.ytimg.com/vi/mBw3qzf4s18/hqdefault.jpg",
        coverUrl = "https://www.writeups.org/wp-content/uploads/Skeletor-Masters-Universe-cartoon-h4-havok-staff.jpg",
        userDisplayName = "Prince Adam",
        website = "https://www.imdb.com/title/tt0093507/",
        internetIdentifier = "he-man@masters-of-the-universe.com"
    )
