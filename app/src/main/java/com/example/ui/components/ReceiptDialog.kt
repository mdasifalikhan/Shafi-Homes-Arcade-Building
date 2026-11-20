package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MaintenanceDue
import com.example.ui.theme.StatusPaidGreen

@Composable
fun ReceiptDialog(
    due: MaintenanceDue,
    societyName: String = "Shafi Homes Arcade",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Receipt,
                    contentDescription = null,
                    tint = StatusPaidGreen
                )
                Column {
                    Text(societyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Official Maintenance Receipt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = StatusPaidGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StatusPaidGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Receipt Number", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = due.receiptNumber ?: "RCP-${due.id.take(8).uppercase()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaidGreen
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StatusPaidGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PAID", fontWeight = FontWeight.Bold, color = StatusPaidGreen)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Billed To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(due.residentName, fontWeight = FontWeight.SemiBold)
                        Text("Unit: ${due.unitNumber}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Billing Period", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(due.billingPeriod, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = due.paidAtEpochMillis?.let { formatEpochDateTime(it) } ?: "Paid",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Breakdown items
                ReceiptLineItem("Base Maintenance Fee", formatCurrency(due.baseAmount))
                ReceiptLineItem("Sinking Fund Contribution", formatCurrency(due.sinkingFund))
                ReceiptLineItem("Utilities & Common Area", formatCurrency(due.utilityAmount))
                if (due.lateFee > 0.0) {
                    ReceiptLineItem("Late Fee Penalty", formatCurrency(due.lateFee))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount Paid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        formatCurrency(due.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusPaidGreen
                    )
                }

                Text(
                    text = "Paid via: ${due.paymentMethodEnum?.label ?: "UPI / Direct"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { sharePaymentReceipt(context, due, societyName) }
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share on WhatsApp")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ReceiptLineItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

fun launchUpiPayment(context: Context, due: MaintenanceDue, societyName: String = "Shafi Homes Arcade") {
    val cleanSociety = societyName.replace(" ", "+")
    val vpa = "shafihomes@upi"
    val note = "Maint+Due+${due.unitNumber}+${due.billingPeriod}".replace(" ", "+")
    val uriString = "upi://pay?pa=$vpa&pn=$cleanSociety&am=${due.totalAmount}&cu=INR&tn=$note"

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    val chooser = Intent.createChooser(intent, "Pay ₹${due.totalAmount} via UPI")
    try {
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "No UPI app found. Please install Google Pay, PhonePe, or Paytm.", Toast.LENGTH_LONG).show()
    }
}

fun sharePaymentReceipt(context: Context, due: MaintenanceDue, societyName: String = "Shafi Homes Arcade") {
    val dateStr = due.paidAtEpochMillis?.let { formatEpochDateTime(it) } ?: "Paid"
    val receiptNo = due.receiptNumber ?: "RCP-${due.id.take(8).uppercase()}"

    val receiptText = """
        🏢 *$societyName*
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        🧾 *MAINTENANCE PAYMENT RECEIPT*
        Receipt No: *$receiptNo*
        Date: $dateStr
        ────────────────────────────
        Flat / Unit: *${due.unitNumber}*
        Resident: *${due.residentName}*
        Period: *${due.billingPeriod}*
        ────────────────────────────
        • Base Maintenance: ₹${due.baseAmount}
        • Sinking Fund: ₹${due.sinkingFund}
        • Utilities & Water: ₹${due.utilityAmount}
        ${if (due.lateFee > 0.0) "• Late Fee: ₹${due.lateFee}\n" else ""}────────────────────────────
        💰 *TOTAL PAID: ₹${due.totalAmount}*
        Payment Mode: ${due.paymentMethodEnum?.label ?: "UPI / Direct"}
        Status: Verified & Settled ✅
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        _Thank you for supporting our community!_
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, receiptText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt via WhatsApp / SMS"))
}
