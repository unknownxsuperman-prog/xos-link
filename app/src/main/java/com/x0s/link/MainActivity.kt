package com.x0s.link

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.x0s.link.data.repository.FavouritesRepository
import com.x0s.link.data.repository.ProfileRepository
import com.x0s.link.ui.colleges.CollegeProfileScreen
import com.x0s.link.ui.colleges.CollegeSearchScreen
import com.x0s.link.ui.favourites.FavouritesScreen
import com.x0s.link.ui.favourites.FavouritesViewModel
import com.x0s.link.ui.feed.FeedScreen
import com.x0s.link.ui.feed.FeedViewModel
import com.x0s.link.ui.messages.ChatScreen
import com.x0s.link.ui.messages.MessagesListScreen
import com.x0s.link.ui.messages.MessagesViewModel
import com.x0s.link.ui.nav.BOTTOM_TABS
import com.x0s.link.ui.nav.Dest
import com.x0s.link.ui.nav.FloatingNavBar
import com.x0s.link.ui.postdetail.PostDetailSheet
import com.x0s.link.ui.profile.EditProfileScreen
import com.x0s.link.ui.profile.ProfileScreen
import com.x0s.link.ui.profile.ProfileViewModel
import com.x0s.link.ui.search.DishaSearchScreen
import com.x0s.link.ui.search.DishaSearchViewModel
import com.x0s.link.ui.theme.XosLinkTheme
import com.x0s.link.ui.zynx.ZynxScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as XosApplication

        setContent {
            var darkTheme by remember { mutableStateOf(true) }
            XosLinkTheme(darkTheme = darkTheme) {
                XosApp(app.profileRepository, app.favouritesRepository, darkTheme) { darkTheme = it }
            }
        }
    }
}

private class VmFactory(private val block: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = block() as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XosApp(
    profileRepo: ProfileRepository,
    favRepo: FavouritesRepository,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    var meUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        profileRepo.ensureLoaded()
        meUserId = profileRepo.defaultUserId()
    }

    val myId = meUserId
    if (myId == null) {
        // Bundled fallback data loads in tens of milliseconds, so this is only ever visible
        // for an instant - but we still show a proper loading state (not a blank frame) in
        // case the device is unusually slow.
        Scaffold { padding ->
            androidx.compose.foundation.layout.Box(
                Modifier.padding(padding).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }
        }
        return
    }

    val feedVm: FeedViewModel = viewModel(factory = VmFactory { FeedViewModel(profileRepo) { myId } })
    val dishaVm: DishaSearchViewModel = viewModel(factory = VmFactory { DishaSearchViewModel(profileRepo) })
    val messagesVm: MessagesViewModel = viewModel(factory = VmFactory { MessagesViewModel() })
    val profileVm: ProfileViewModel = viewModel(factory = VmFactory { ProfileViewModel(profileRepo) })
    val favVm: FavouritesViewModel = viewModel(factory = VmFactory { FavouritesViewModel(favRepo) })

    var postDetail by remember { mutableStateOf<Pair<Int, List<Pair<com.x0s.link.data.model.XosProfile, com.x0s.link.data.model.XosPost>>>?>(null) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = BOTTOM_TABS.any { it.route == currentRoute }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Dest.FEED,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Dest.FEED) {
                FeedScreen(
                    vm = feedVm,
                    myAvatar = profileRepo.getProfile(myId)?.avatar,
                    onOpenDrawer = { navController.navigate(Dest.profile(myId)) },
                    onOpenProfile = { navController.navigate(Dest.profile(it)) },
                    onOpenPost = { index -> postDetail = index to feedVm.posts.value }
                )
            }
            composable(Dest.SEARCH) {
                DishaSearchScreen(
                    vm = dishaVm,
                    myFirstName = (profileRepo.getProfile(myId)?.displayName ?: "there").substringBefore(" "),
                    onOpenProfile = { navController.navigate(Dest.profile(it)) },
                    onOpenCollege = { navController.navigate(Dest.collegeProfile(it)) }
                )
            }
            composable(Dest.ZYNX) {
                ZynxScreen(repo = profileRepo, onOpenProfile = { navController.navigate(Dest.profile(it)) })
            }
            composable(Dest.MESSAGES) {
                MessagesListScreen(vm = messagesVm, onOpenChat = { navController.navigate(Dest.chat(it)) })
            }
            composable(Dest.CHAT) { backStack ->
                val id = backStack.arguments?.getString("convId")?.toIntOrNull() ?: return@composable
                ChatScreen(vm = messagesVm, convId = id, onBack = { navController.popBackStack() })
            }
            composable(Dest.NOTIFICATIONS) {
                androidx.compose.foundation.layout.Box(
                    Modifier.padding(24.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) { Text("Notifications coming soon", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            composable(Dest.PROFILE) { backStack ->
                val userId = backStack.arguments?.getString("userId") ?: myId
                ProfileScreen(
                    userId = userId,
                    vm = profileVm,
                    meUserId = myId,
                    onBack = if (userId != myId) ({ navController.popBackStack() }) else null,
                    onEditProfile = { navController.navigate(Dest.EDIT_PROFILE) },
                    onOpenFavourites = { navController.navigate(Dest.favourites(userId)) },
                    onOpenCollegeSearch = { navController.navigate(Dest.COLLEGE_SEARCH) },
                    onOpenCollegeProfile = { navController.navigate(Dest.collegeProfile(it)) },
                    onOpenPost = { index ->
                        val profile = profileRepo.getProfile(userId)
                        if (profile != null) {
                            postDetail = index to profile.posts.map { profile to it }
                        }
                    }
                )
            }
            composable(Dest.EDIT_PROFILE) {
                EditProfileScreen(vm = profileVm, onBack = { navController.popBackStack() })
            }
            composable(Dest.COLLEGE_SEARCH) {
                CollegeSearchScreen(
                    repo = profileRepo,
                    onBack = { navController.popBackStack() },
                    onSelect = { college ->
                        profileVm.linkCollege(college)
                        navController.popBackStack()
                    }
                )
            }
            composable(Dest.COLLEGE_PROFILE) { backStack ->
                val id = backStack.arguments?.getString("collegeId") ?: ""
                CollegeProfileScreen(
                    college = profileRepo.getCollege(id),
                    onBack = { navController.popBackStack() },
                    onFollowCollege = {}
                )
            }
            composable(Dest.FAVOURITES) { backStack ->
                val userId = backStack.arguments?.getString("userId") ?: myId
                FavouritesScreen(vm = favVm, userId = userId, onBack = { navController.popBackStack() })
            }
        }

        if (showBottomBar) {
            FloatingNavBar(
                currentRoute = currentRoute,
                hasUnreadNotifications = true,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
    }

    postDetail?.let { (index, posts) ->
        val (profile, post) = posts.getOrNull(index) ?: run { postDetail = null; return@let }
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        var liked by remember(index) { mutableStateOf(false) }
        LaunchedEffect(index) { liked = profileRepo.prefs.isLiked(myId, "${profile.userid}_$index") }
        PostDetailSheet(
            profile = profile,
            post = post,
            liked = liked,
            onToggleLike = {
                scope.launch {
                    liked = profileRepo.prefs.toggleLike(myId, "${profile.userid}_$index")
                }
            },
            onDismiss = { postDetail = null }
        )
    }
}

