package com.rivo.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoTopBar
import com.rivo.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit
) {
    var issueDescription by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedIssueType by remember { mutableStateOf("Technical Issue") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            RivoTopBar(
                title = "Help & Support",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // FAQ Section
            Text(
                text = "Frequently Asked Questions",
                color = Primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FaqItem(
                question = "How do I upload my music?",
                answer = "Go to your Artist Dashboard and click on 'Upload Music'. Follow the instructions to upload your track, add artwork, and set details."
            )

            FaqItem(
                question = "How do I get verified as an artist?",
                answer = "Navigate to your profile and click on 'Get Verified'. Complete the verification form and submit the required documents. Our team will review your application within 7 days."
            )

            FaqItem(
                question = "How are royalties calculated?",
                answer = "Royalties are calculated based on the number of streams your music receives. You can view detailed analytics in your Artist Dashboard."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Support Section
            Text(
                text = "Contact Support",
                color = Primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Issue Type
            Text(
                text = "Issue Type",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            var issueTypeExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222), shape = MaterialTheme.shapes.small)
                    .padding(4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = issueTypeExpanded,
                    onExpandedChange = { issueTypeExpanded = !issueTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedIssueType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issueTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Primary,
                            focusedContainerColor = Color(0xFF222222),
                            unfocusedContainerColor = Color(0xFF222222)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = issueTypeExpanded,
                        onDismissRequest = { issueTypeExpanded = false },
                        modifier = Modifier.background(Color(0xFF222222))
                    ) {
                        listOf("Technical Issue", "Account Problem", "Payment Issue", "Copyright Claim", "Other").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    selectedIssueType = option
                                    issueTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Issue Description
            Text(
                text = "Describe Your Issue",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = issueDescription,
                onValueChange = { issueDescription = it },
                placeholder = { Text("Please provide details about your issue...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF222222),
                    unfocusedContainerColor = Color(0xFF222222)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            Text(
                text = "Your Email",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("your@email.com", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF222222),
                    unfocusedContainerColor = Color(0xFF222222)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { /* Submit support ticket */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Submit Ticket",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Contact Information",
                        color = Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Email: support@rivo.com",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Phone: +251 11 123 4567",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Hours: Monday-Friday, 9AM-5PM EAT",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expanded) Primary else Color(0xFF333333),
                        contentColor = if (expanded) Color.Black else Color.White
                    ),
                    modifier = Modifier.size(30.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "-" else "+",
                        fontSize = 18.sp
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}
