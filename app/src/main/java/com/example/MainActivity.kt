package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.SocietyRepository
import com.example.ui.SocietyDashboardState
import com.example.ui.SocietyViewModel
import com.example.ui.UiState
import com.example.ui.navigation.AppDestination
import com.example.ui.navigation.MaintenanceRequestsRoutes
import com.example.ui.navigation.ShafiHomesNavHost
import com.example.ui.screens.NewMaintenanceDueSheet
import com.example.ui.screens.ProfileSheet
import com.example.ui.screens.RegisterContractorSheet
import com.example.ui.theme.CivicNestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CivicNestTheme {
                ShafiHomesArcadeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShafiHomesArcadeApp() {
    val viewModel: SocietyViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                SocietyViewModel(SocietyRepository(app))
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val navDestination = backStackEntry?.destination
    val currentDestination = AppDestination.fromRoute(navDestination?.route)
    val isNewRequestScreen = navDestination?.route == MaintenanceRequestsRoutes.NEW_REQUEST

    var showNewDueSheet by remember { mutableStateOf(false) }
    var showAddContractorSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navigateToDestination: (AppDestination) -> Unit = { destination ->
        if (destination == AppDestination.MAINTENANCE_REQUESTS && isNewRequestScreen) {
            navController.popBackStack(MaintenanceRequestsRoutes.LIST, inclusive = false)
        } else {
            navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val loadedState = (uiState as? UiState.Success)?.data
    val destinationLabel = if (isNewRequestScreen) {
        stringResource(id = R.string.title_new_maintenance_request)
    } else {
        stringResource(id = currentDestination.labelRes)
    }
    val societySubtitle = loadedState?.profile?.societyName ?: stringResource(id = R.string.app_name)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = destinationLabel,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = societySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        if (isNewRequestScreen) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.testTag("new_request_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back to Maintenance Requests"
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showProfileSheet = true },
                            modifier = Modifier.testTag("profile_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = stringResource(id = R.string.cd_profile_settings),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!useRail) {
                    SocietyBottomNavigation(
                        isSelected = { destination ->
                            navDestination?.hierarchy?.any { it.route == destination.route } == true ||
                                (navDestination == null && destination == AppDestination.MAINTENANCE_REQUESTS)
                        },
                        onSelectDestination = navigateToDestination
                    )
                }
            },
            floatingActionButton = {
                if (loadedState != null && !isNewRequestScreen) {
                    when (currentDestination) {
                        AppDestination.MAINTENANCE_REQUESTS -> FloatingActionButton(
                            onClick = {
                                navController.navigate(MaintenanceRequestsRoutes.NEW_REQUEST) {
                                    launchSingleTop = true
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.testTag("fab_log_repair")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = stringResource(id = R.string.action_log_repair))
                        }
                        AppDestination.PAYMENT_COLLECTION -> FloatingActionButton(
                            onClick = { showNewDueSheet = true },
                            modifier = Modifier.testTag("fab_new_bill")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = stringResource(id = R.string.action_add_due))
                        }
                        AppDestination.CONTRACTOR_DASHBOARD -> FloatingActionButton(
                            onClick = { showAddContractorSheet = true },
                            modifier = Modifier.testTag("fab_add_contractor")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = stringResource(id = R.string.action_add_contractor))
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        AppDestination.entries.forEach { destination ->
                            val selected = navDestination?.hierarchy?.any { it.route == destination.route } == true
                            val label = stringResource(id = destination.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToDestination(destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.testTag("rail_${destination.route}")
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Unable to load society workspace",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        is UiState.Success -> {
                            val data: SocietyDashboardState = state.data
                            ShafiHomesNavHost(
                                navController = navController,
                                state = data,
                                onSubmitNewRequest = { unit, title, desc, cat, priority, est, photoLabel, photoUri, autoSched ->
                                    viewModel.logRepairTicket(
                                        unitLocation = unit,
                                        title = title,
                                        description = desc,
                                        category = cat,
                                        priority = priority,
                                        estimatedCost = est,
                                        photoLabel = photoLabel,
                                        photoUri = photoUri,
                                        autoScheduleNow = autoSched
                                    )
                                },
                                onAutoScheduleTicket = { ticket -> viewModel.runAutoScheduler(ticket) },
                                onAutoScheduleAll = { viewModel.runAutoScheduler() },
                                onUpdateRepairProgress = { ticket, status, cost, notes ->
                                    viewModel.updateRepairProgress(ticket, status, cost, notes)
                                },
                                onDeleteTicket = { ticketId -> viewModel.deleteRepairTicket(ticketId) },
                                onOpenNewDue = { showNewDueSheet = true },
                                onRecordPayment = { due, method, receipt, notes ->
                                    viewModel.recordPayment(due, method, receipt, notes)
                                },
                                onMarkOverdue = { due, lateFee ->
                                    viewModel.markOverdue(due, lateFee)
                                },
                                onDeleteDue = { dueId -> viewModel.deleteDue(dueId) },
                                onAdvanceScheduleEntry = { entry, next, cost, notes ->
                                    viewModel.advanceScheduleStatus(entry, next, cost, notes)
                                },
                                onRescheduleEntry = { entry, day, slot ->
                                    viewModel.rescheduleVisit(entry, day, slot)
                                },
                                onOpenAddContractor = { showAddContractorSheet = true },
                                onToggleContractorAvailable = { id, avail ->
                                    viewModel.toggleContractorAvailability(id, avail)
                                },
                                onDeleteContractor = { id -> viewModel.deleteContractor(id) },
                                modifier = Modifier
                                    .widthIn(max = 780.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewDueSheet) {
        NewMaintenanceDueSheet(
            defaultUnit = loadedState?.profile?.unitLabel ?: "A-101",
            defaultResident = loadedState?.profile?.residentName ?: "Resident",
            defaultMonthlyFee = loadedState?.profile?.defaultMonthlyFee ?: 3500.0,
            onDismiss = { showNewDueSheet = false },
            onSubmit = { unit, resident, period, base, sink, util, epochDay, notes ->
                viewModel.createDue(unit, resident, period, base, sink, util, epochDay, notes)
                showNewDueSheet = false
            }
        )
    }

    if (showAddContractorSheet) {
        RegisterContractorSheet(
            onDismiss = { showAddContractorSheet = false },
            onSubmit = { name, company, spec, phone, startH, endH, cap, rate ->
                viewModel.registerContractor(name, company, spec, phone, startH, endH, cap, rate)
                showAddContractorSheet = false
            }
        )
    }

    if (showProfileSheet) {
        ProfileSheet(
            profile = loadedState?.profile,
            onDismiss = { showProfileSheet = false },
            onSaveProfile = { soc, res, unit, fee, role ->
                viewModel.saveProfile(soc, res, unit, fee, role)
                showProfileSheet = false
            }
        )
    }
}

@Composable
internal fun SocietyBottomNavigation(
    isSelected: (AppDestination) -> Boolean,
    onSelectDestination: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("society_bottom_navigation"),
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AppDestination.entries.forEach { destination ->
            val selected = isSelected(destination)
            val label = stringResource(id = destination.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(destination.navTestTag)
            )
        }
    }
}

