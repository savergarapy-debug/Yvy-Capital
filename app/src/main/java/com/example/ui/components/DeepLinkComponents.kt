package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Property
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DeepLinkHandler

/**
 * Diálogo para compartir una propiedad mediante el menú nativo de Android (Intent.ACTION_SEND)
 * incluyendo el nombre de la propiedad, información principal y su enlace directo verificable.
 */
@Composable
fun SharePropertyDirectLinkDialog(
    property: Property,
    displayCurrency: String,
    onDismiss: () -> Unit,
    onLinkCopied: (String) -> Unit,
    onOpenWebFallbackPreview: () -> Unit
) {
    val context = LocalContext.current
    val httpsLink = remember(property.id) { DeepLinkHandler.buildPropertyLink(property.id) }
    val customSchemeLink = remember(property.id) { DeepLinkHandler.buildPropertyCustomSchemeLink(property.id) }
    val formattedPrice = remember(property.price, property.currency, displayCurrency) {
        CurrencyFormatter.formatPrice(property.price, property.currency, displayCurrency)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = YvyForestPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Compartir Propiedad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = YvyForestPrimary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${property.neighborhood}, ${property.city} (${property.department})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$formattedPrice • ${property.areaTotalM2} m² • ${property.bedrooms} dorm.",
                            style = MaterialTheme.typography.labelLarge,
                            color = YvyForestPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Comparte la ficha completa con su nombre, precio, ubicación y enlace directo para abrirla en Yvy Capital:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "App Link (HTTPS): $httpsLink",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Esquema Nativo: $customSchemeLink",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        DeepLinkHandler.shareProperty(context, property, displayCurrency)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YvyForestPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_native_chooser_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir con Menú de Android")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            DeepLinkHandler.copyToClipboard(context, "Enlace Yvy Capital", httpsLink)
                            onLinkCopied("Enlace directo copiado: $httpsLink")
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_https_link_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Enlace")
                    }

                    OutlinedButton(
                        onClick = onOpenWebFallbackPreview,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_case_b_btn")
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vista Web (Caso B)")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Caso C: El contenido compartido fue eliminado, pausado o ya no existe.
 */
@Composable
fun DeepLinkNotFoundDialog(
    rawUrl: String,
    title: String,
    message: String,
    onGoHome: () -> Unit,
    onExploreProperties: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (rawUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enlace consultado: $rawUrl",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                Text(
                    text = "Puedes volver a la pantalla de inicio o explorar otras propiedades verificadas en Paraguay.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onExploreProperties,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YvyForestPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("deeplink_not_found_explore_btn")
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Explorar Propiedades")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.testTag("deeplink_not_found_home_btn")
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Volver al Inicio")
            }
        }
    )
}

/**
 * Caso B: Vista previa de la página pública de destino cuando el destinatario no tiene instalada la app.
 */
@Composable
fun PublicWebFallbackPreviewDialog(
    property: Property,
    displayCurrency: String,
    onOpenDirectlyInApp: () -> Unit,
    onCopyDeferredLink: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val httpsLink = remember(property.id) { DeepLinkHandler.buildPropertyLink(property.id) }
    val priceText = remember(property.price, property.currency, displayCurrency) {
        CurrencyFormatter.formatPrice(property.price, property.currency, displayCurrency)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = YvyGoldAccent,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Página Pública de Destino (Caso B)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Así ve el enlace un destinatario que aún no tiene Yvy Capital instalada en su teléfono:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = YvyForestDeep),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "YVY CAPITAL PARAGUAY • WEB PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = YvyGoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${property.neighborhood}, ${property.city} • $priceText",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${property.areaTotalM2} m² • ${property.bedrooms} dormitorios • ${property.bathrooms} baños",
                            style = MaterialTheme.typography.labelSmall,
                            color = YvyGoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onOpenDirectlyInApp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YvyGoldAccent,
                                contentColor = YvyForestDeep
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir en Yvy Capital", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                DeepLinkHandler.copyToClipboard(context, "Enlace Diferido Yvy Capital", httpsLink)
                                onCopyDeferredLink(httpsLink)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar enlace para abrir tras instalar", color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Centro interactivo para abrir un enlace recibido o verificar los Casos A, B y C.
 */
@Composable
fun DeepLinkCenterDialog(
    activeProperties: List<Property>,
    displayCurrency: String,
    onResolveLink: (String) -> Unit,
    onPreviewWebFallback: (Property) -> Unit,
    onDismiss: () -> Unit
) {
    val firstProp = activeProperties.firstOrNull()
    val sampleValidLink = remember(firstProp?.id) {
        DeepLinkHandler.buildPropertyLink(firstProp?.id ?: "py_asuncion_villamorra_01")
    }
    var customLinkInput by remember(sampleValidLink) { mutableStateOf(sampleValidLink) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = YvyForestPrimary
            )
        },
        title = {
            Text(
                text = "Enlaces Directos • Yvy Capital",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(380.dp)
            ) {
                item {
                    Text(
                        text = "Pega un enlace compartido de Yvy Capital o prueba cualquiera de los escenarios reales:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = customLinkInput,
                        onValueChange = { customLinkInput = it },
                        label = { Text("Enlace o ID de propiedad") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deeplink_input_field")
                    )
                }

                item {
                    Button(
                        onClick = { onResolveLink(customLinkInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YvyForestPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deeplink_open_btn")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir Enlace Directamente en la App")
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    Text(
                        text = "Escenarios de Verificación Rápida:",
                        style = MaterialTheme.typography.labelLarge,
                        color = YvyForestPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (firstProp != null) {
                    item {
                        OutlinedButton(
                            onClick = {
                                onResolveLink(DeepLinkHandler.buildPropertyLink(firstProp.id))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_case_a_btn")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Caso A: Abrir \"${firstProp.title.take(24)}...\"")
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = { onPreviewWebFallback(firstProp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_case_b_btn")
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Caso B: Destinatario sin la App instalada")
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            onResolveLink("https://yvycapital.com.py/propiedad/prop_eliminada_999")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_case_c_btn")
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Caso C: Propiedad eliminada o no disponible")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
