package com.dev.demoapplication.presentation


import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.demoapplication.R
import com.dev.demoapplication.utils.getDeviceId
import com.example.link.screens.HomeActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navigateToHome: () -> Unit)
        /* navigateToSignup: () -> Unit,
         navigateToHome: () -> Unit*/ {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_username),
                contentDescription = "Login Illustration",
                modifier = Modifier.size(221.dp, 151.dp)
            )
            Text(
                text = "Welcome back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Sign in to access your account",
                fontSize = 14.sp,
                color = Color.Gray
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter your email", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = null
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Black
                ),
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_password),
                        contentDescription = null
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Black
                ),
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
            )
            getDeviceId(LocalContext.current)
            Button(
                onClick = {
                    // Hardcoded credentials check
                    if (email == "test" && password == "123456") {
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 15.dp, end = 15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_200)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Next", color = Color.White, fontSize = 18.sp)
            }

            /* TextButton(onClick = navigateToSignup) {
                 Text("New member? ", color = Color.Gray, fontSize = 14.sp)
                 Text("Register now", color = Color.Red, fontSize = 14.sp)
             }*/
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(navigateToHome = {})
}
