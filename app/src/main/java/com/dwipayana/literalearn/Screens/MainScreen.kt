package com.dwipayana.literalearn.Screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dwipayana.literalearn.NavItem
import com.dwipayana.literalearn.pages.BelajarPage
import com.dwipayana.literalearn.pages.HomePage
import com.dwipayana.literalearn.pages.ProfilPage
import com.dwipayana.literalearn.pages.SimpanPage

import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwipayana.literalearn.ui.viewmodel.SubjectViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier, 
    onLogout: () -> Unit = {},
    subjectViewModel: SubjectViewModel = viewModel()
) {
    val subjects by subjectViewModel.subjects.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.dwipayana.literalearn.data.network.SessionManager(context) }
    
    LaunchedEffect(Unit) {
        val userUuid = sessionManager.getUserUuid()
        if (userUuid != null) {
            subjectViewModel.fetchSubjectsWithProgress(userUuid)
        }
    }

    val navItemList = listOf(
        NavItem(label = "Beranda", Icons.Default.Home),
        NavItem(label = "Belajar", Icons.AutoMirrored.Filled.MenuBook),
        NavItem(label = "Simpan", Icons.Default.Bookmark),
        NavItem(label = "Profil", Icons.Default.Person),
    )
    
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedSubjectTitle by remember { mutableStateOf<String?>(null) }
    var selectedSubjectUuid by remember { mutableStateOf<String?>(null) }
    var selectedModuleUuid by remember { mutableStateOf<String?>(null) }
    var selectedModuleTitle by remember { mutableStateOf<String?>(null) }
    var selectedModuleOrder by remember { mutableIntStateOf(1) }
    var selectedMaterialUuid by remember { mutableStateOf<String?>(null) }
    var selectedQuizUuid by remember { mutableStateOf<String?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Logika Navigasi Berlapis
    when {
        isEditingProfile -> {
            EditProfileScreen(
                onBack = { isEditingProfile = false },
                onSaveSuccess = { isEditingProfile = false }
            )
        }
        selectedQuizUuid != null -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = remember { com.dwipayana.literalearn.data.network.SessionManager(context) }
            KuisScreen(
                quizUuid = selectedQuizUuid!!,
                onBack = { 
                    selectedQuizUuid = null
                    selectedSubjectTitle = null // Reset agar kembali ke Home, bukan MateriScreen
                    selectedSubjectUuid = null
                    selectedIndex = 0 // Paksa pindah ke tab Beranda
                },
                onQuizPassed = { actualScore ->
                    val userUuid = sessionManager.getUserUuid()
                    
                    // --- SIMPAN SKOR SESUAI BAB ---
                    sessionManager.saveScoreByOrder(selectedModuleOrder, actualScore.toFloat(), selectedModuleTitle)
                    
                    // --- TAMBAH TOTAL POIN ---
                    sessionManager.addPoints(actualScore)

                    if (userUuid != null && selectedSubjectUuid != null) {
                        val currentSubject = subjectViewModel.subjects.value.find { it.uuid == selectedSubjectUuid }
                        val currentProg = currentSubject?.currentProgress ?: 0.0
                        
                        // Milestone Kuis: Bab 1 -> 50%, Bab 2 -> 100%
                        val milestone = if (selectedModuleOrder >= 2) 100.0 else 50.0
                        // Setiap aksi kuis naik 25%, tapi tidak boleh melebihi milestonenya
                        val targetProg = (currentProg + 25.0).coerceAtMost(milestone).toInt()
                        
                        android.util.Log.d("ProgressSync", "KUIS SELESAI di Bab $selectedModuleOrder. Current: $currentProg, Target: $targetProg")
                        
                        if (currentProg < targetProg) {
                            subjectViewModel.updateSubjectProgress(userUuid, selectedSubjectUuid!!, targetProg)
                        }
                    }
                }
            )
        }
        selectedMaterialUuid != null -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = remember { com.dwipayana.literalearn.data.network.SessionManager(context) }
            MaterialDetailScreen(
                materialUuid = selectedMaterialUuid!!,
                onBack = { 
                    val userUuid = sessionManager.getUserUuid()
                    if (userUuid != null && selectedSubjectUuid != null) {
                        val currentSubject = subjectViewModel.subjects.value.find { it.uuid == selectedSubjectUuid }
                        val currentProg = currentSubject?.currentProgress ?: 0.0
                        
                        // Milestone Materi: Bab 1 -> 25%, Bab 2 -> 75%
                        val milestone = if (selectedModuleOrder >= 2) 75.0 else 25.0
                        // Setiap aksi baca materi naik 25%, tapi tidak boleh melebihi milestonenya
                        val targetProg = (currentProg + 25.0).coerceAtMost(milestone).toInt()
                        
                        android.util.Log.d("ProgressSync", "BACA SELESAI di Bab $selectedModuleOrder. Current: $currentProg, Target: $targetProg")
                        
                        if (currentProg < targetProg) {
                            subjectViewModel.updateSubjectProgress(userUuid, selectedSubjectUuid!!, targetProg)
                        }
                    }
                    selectedMaterialUuid = null 
                }
            )
        }
        selectedModuleUuid != null -> {
            MaterialListScreen(
                moduleUuid = selectedModuleUuid!!,
                moduleTitle = selectedModuleTitle ?: "Daftar Materi",
                onBack = { 
                    selectedModuleUuid = null
                    selectedModuleTitle = null
                },
                onMaterialClick = { uuid -> selectedMaterialUuid = uuid }
            )
        }
        selectedSubjectTitle != null -> {
            val currentProgress = subjects.find { it.uuid == selectedSubjectUuid }?.currentProgress ?: 0.0
            MateriScreen(
                mapel = selectedSubjectTitle!!,
                subjectUuid = selectedSubjectUuid,
                currentProgress = currentProgress,
                onBack = { 
                    selectedSubjectTitle = null
                    selectedSubjectUuid = null
                },
                onStartQuiz = { quizUuid, title, order -> 
                    selectedQuizUuid = quizUuid 
                    selectedModuleTitle = title
                    selectedModuleOrder = order
                },
                onModuleClick = { uuid, title, order ->
                    selectedModuleUuid = uuid
                    selectedModuleTitle = title
                    selectedModuleOrder = order
                }
            )
        }
        else -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar {
                        navItemList.forEachIndexed { index, navItem ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                                label = { Text(text = navItem.label) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                ContentScreen(
                    modifier = Modifier.padding(innerPadding),
                    selectedIndex = selectedIndex,
                    onLogout = onLogout,
                    onSubjectSelected = { title, uuid -> 
                        selectedSubjectTitle = title
                        selectedSubjectUuid = uuid
                    },
                    subjectsList = subjects,
                    onEditProfileSelected = { isEditingProfile = true },
                    subjectViewModel = subjectViewModel,
                    onMaterialSelected = { materialUuid -> selectedMaterialUuid = materialUuid }
                )
            }
        }
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onLogout: () -> Unit,
    onSubjectSelected: (String, String?) -> Unit,
    subjectsList: List<com.dwipayana.literalearn.data.model.Subject>,
    onEditProfileSelected: () -> Unit,
    subjectViewModel: SubjectViewModel,
    onMaterialSelected: (String) -> Unit // Tambahkan ini
) {
    when (selectedIndex) {
        0 -> HomePage(
            modifier = modifier, 
            onSubjectClick = { title -> 
                val realUuid = subjectsList.find { it.title.contains(title, ignoreCase = true) }?.uuid
                onSubjectSelected(title, realUuid)
            },
            subjects = subjectsList
        )
        1 -> BelajarPage(
            modifier = modifier, 
            onSubjectClick = { title -> 
                val realUuid = subjectsList.find { it.title.contains(title, ignoreCase = true) }?.uuid
                onSubjectSelected(title, realUuid)
            },
            viewModel = subjectViewModel
        )
        2 -> SimpanPage(
            modifier = modifier, 
            onSubjectClick = { materialUuid -> 
                onMaterialSelected(materialUuid)
            }
        )
        3 -> ProfilPage(
            modifier = modifier, 
            onLogoutClick = onLogout,
            onEditProfileClick = { onEditProfileSelected() }
        )
    }
}
