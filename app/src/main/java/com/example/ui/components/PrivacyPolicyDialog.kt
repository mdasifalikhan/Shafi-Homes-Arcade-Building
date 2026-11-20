package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun PrivacyPolicyDialog(
    societyName: String = "Shafi Homes Arcade",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFF59E0B), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_building_logo),
                    contentDescription = "Shafi Homes Arcade Building",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = societyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Effective: November 2026 • Google Play Compliant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                PolicySection(
                    title = "1. Introduction",
                    body = "$societyName operates as a residential building society management application. We prioritize resident privacy and transparent data usage."
                )

                PolicySection(
                    title = "2. Information We Collect",
                    body = "• Flat & Resident Information: Unit number, resident name, and maintenance assessment records.\n" +
                        "• Visitor Logs: Guest name, contact number (optional), purpose of visit, and entry/exit timestamps for building security.\n" +
                        "• Maintenance Tickets: Trade category, issue description, priority, and optional photos attached by residents."
                )

                PolicySection(
                    title = "3. Payment & Financial Data",
                    body = "Payment actions initiated via UPI (Google Pay, PhonePe, Paytm, etc.) are processed directly by installed, certified UPI banking applications. This app never accesses, records, or stores your UPI PIN, card numbers, or bank account credentials."
                )

                PolicySection(
                    title = "4. Data Storage & Sharing",
                    body = "All your society records, visitor passes, and maintenance logs are stored securely in your device's local database. We do not sell, rent, or monetize your personal data with third-party advertisers."
                )

                PolicySection(
                    title = "5. Permissions",
                    body = "• INTERNET: Used exclusively for network connectivity, optional UPI payment deep-linking, and Firebase services.\n" +
                        "• We do NOT request background location, SMS, or contacts permissions."
                )

                PolicySection(
                    title = "6. Data Retention & Deletion",
                    body = "Residents and management committee members have full control to edit or delete maintenance dues, visitor passes, notices, and repair tickets directly inside the application at any time."
                )

                PolicySection(
                    title = "7. Contact & Support",
                    body = "If you have questions regarding this Privacy Policy, please contact the $societyName Management Office or email asifalikhan@programmer.com."
                )
            }
        },
        confirmButton = {
            Button(onClick = { sharePrivacyPolicy(context, societyName) }) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Policy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

private fun sharePrivacyPolicy(context: Context, societyName: String) {
    val policyText = """
        🔒 *$societyName - PRIVACY POLICY SUMMARY*
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        • Local & Secure: All maintenance dues, guest passes, and repair tickets are securely managed for building society operations.
        • No Third-Party Selling: Personal data is never sold or shared with advertisers.
        • Safe UPI Payments: All payments are routed directly to certified UPI banking apps (Google Pay, PhonePe, Paytm). No banking credentials or PINs are ever stored.
        • User Control: Records can be viewed, updated, or removed directly in the app.
        
        For inquiries, contact: asifalikhan@programmer.com
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, policyText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Privacy Policy"))
}
