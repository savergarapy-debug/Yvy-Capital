package com.example.ui.auth

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StaffUser
import com.example.ui.theme.YvyEditorialSerifFamily
import com.example.ui.theme.YvySansFamily
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun StaffLoginScreen(
    onBack: () -> Unit,
    onStaffLoginSuccess: (StaffUser) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Bloqueo temporal tras 5 intentos fallidos
    var failedAttempts by remember { mutableIntStateOf(0) }
    var lockoutSecondsRemaining by remember { mutableIntStateOf(0) }

    // Cuenta regresiva del bloqueo
    LaunchedEffect(lockoutSecondsRemaining) {
        if (lockoutSecondsRemaining > 0) {
            delay(1000L)
            lockoutSecondsRemaining -= 1
            if (lockoutSecondsRemaining == 0) {
                failedAttempts = 0
            }
        }
    }

    fun submitStaffLogin() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            errorMessage = "Ingresa tu correo corporativo y contraseña."
            return
        }

        if (lockoutSecondsRemaining > 0) return

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                // a) Iniciar sesión con Firebase Auth (correo y contraseña)
                val authResult = Firebase.auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                val user = authResult.user

                if (user == null) {
                    failedAttempts++
                    if (failedAttempts >= 5) {
                        lockoutSecondsRemaining = 30
                    }
                    errorMessage = "Usuario o contraseña incorrectos"
                    isLoading = false
                    return@launch
                }

                // c) Verificar documento staff/{uid} en Firestore con active == true y role: 'admin' | 'staff'
                val db = try {
                    val dbId = context.getString(R.string.firestore_database_id)
                    if (dbId.isNotBlank() && dbId != "(default)") {
                        FirebaseFirestore.getInstance(dbId)
                    } else {
                        FirebaseFirestore.getInstance()
                    }
                } catch (_: Exception) {
                    FirebaseFirestore.getInstance()
                }

                val staffDoc = db.collection("staff").document(user.uid).get().await()

                val isActive = staffDoc.getBoolean("active") == true
                val role = staffDoc.getString("role")

                if (!staffDoc.exists() || !isActive || (role != "admin" && role != "staff")) {
                    // Cerrar sesión inmediatamente si no es personal autorizado
                    Firebase.auth.signOut()
                    failedAttempts++
                    if (failedAttempts >= 5) {
                        lockoutSecondsRemaining = 30
                    }
                    errorMessage = "Esta cuenta no tiene acceso de personal"
                    isLoading = false
                    return@launch
                }

                // Ingreso autorizado
                failedAttempts = 0
                val staffName = staffDoc.getString("name")
                    ?: staffDoc.getString("displayName")
                    ?: user.email?.substringBefore("@")
                    ?: "Personal Yvy"

                val staffUser = StaffUser(
                    uid = user.uid,
                    name = staffName,
                    role = role ?: "staff",
                    email = user.email ?: trimmedEmail,
                    active = true
                )

                isLoading = false
                onStaffLoginSuccess(staffUser)

            } catch (e: Exception) {
                // f) Mensajes de error genéricos para evitar enumeración de cuentas
                failedAttempts++
                if (failedAttempts >= 5) {
                    lockoutSecondsRemaining = 30
                }
                errorMessage = "Usuario o contraseña incorrectos"
                isLoading = false
            }
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = YvySpacing.ScreenHorizontal, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra superior de navegación atrás
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("staff_login_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver al ingreso general",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Volver",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.widthIn(max = 440.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícono corporativo
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = YvyShapes.Card,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Acceso de Personal",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = YvyEditorialSerifFamily,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Portal interno para colaboradores de Yvy Capital",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = YvySansFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Tarjeta de formulario de ingreso
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Iniciar sesión corporativa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Campo de correo corporativo
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    if (errorMessage != null) errorMessage = null
                                },
                                label = { Text("Correo corporativo") },
                                placeholder = { Text("nombre@yvycapital.com.py") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                shape = YvyShapes.FieldAndButton,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("staff_login_email_input")
                            )

                            // Campo de contraseña con botón mostrar/ocultar
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    if (errorMessage != null) errorMessage = null
                                },
                                label = { Text("Contraseña") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { submitStaffLogin() }
                                ),
                                shape = YvyShapes.FieldAndButton,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("staff_login_password_input")
                            )

                            // Mensaje de error
                            if (errorMessage != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = YvyShapes.FieldAndButton,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            // Botón de ingreso con bloqueo de 30s tras 5 intentos
                            val isBlocked = lockoutSecondsRemaining > 0
                            val canClick = !isLoading && !isBlocked

                            Button(
                                onClick = { submitStaffLogin() },
                                enabled = canClick,
                                shape = YvyShapes.FieldAndButton,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White,
                                    disabledContainerColor = if (isBlocked) {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    },
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("staff_login_submit_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Verificando credenciales...")
                                } else if (isBlocked) {
                                    Text("Reintentar en ${lockoutSecondsRemaining}s")
                                } else {
                                    Text(
                                        text = "Ingresar",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Texto informativo obligatorio
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Las credenciales son proporcionadas por Yvy Capital. Si no tenés acceso, contactá a administración.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
