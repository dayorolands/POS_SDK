package com.creditclub.pos.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.creditclub.core.data.api.FundsTransferService
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.getMessage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.setResult
import com.creditclub.ui.CreditClubAppBar
import com.creditclub.ui.ErrorFeedback
import com.creditclub.ui.rememberBean
import com.creditclub.ui.rememberRetrofitService

@Composable
fun GetBank(
    title: String,
    popOnSelect: Boolean = true,
    onResult: ((Bank) -> Unit)? = null,
    navController: NavHostController
) {
    val context = LocalContext.current
    val fundsTransferService: FundsTransferService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var query by rememberSaveable { mutableStateOf("") }
    val (loading, setLoading) = remember { mutableStateOf(false) }
    val (errorMessage, setError) = remember { mutableStateOf("") }
    val issuingBanks by produceState(emptyList<Bank>()) {
        setLoading(true)
        val (response, error) = safeRunIO {
            fundsTransferService.getBanks(localStorage.institutionCode)
        }
        setLoading(false)
        if (error != null) {
            setError(error.getMessage(context))
            value = emptyList()
            return@produceState
        }
        setError("")
        value = response ?: emptyList()
    }
    val filteredIssuingBanks = remember(query, issuingBanks) {
        if (query.isBlank()) issuingBanks
        else issuingBanks.filter { it.name?.contains(query) ?: false }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = title,
            onBackPressed = { navController.popBackStack() },
            modifier = Modifier.constrainAs(appBar) {
                top.linkTo(parent.top)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(text = "Bank Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        LazyColumn(
            modifier = Modifier.constrainAs(list) {
                top.linkTo(appBar.bottom)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        ) {
            if (loading) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(bottom = 10.dp))
                        Text(
                            text = "Loading Banks",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.onSurface.copy(0.52f),
                        )
                    }
                }
                return@LazyColumn
            }

            if (errorMessage.isNotBlank()) {
                item {
                    ErrorFeedback(errorMessage = errorMessage)
                }
                return@LazyColumn
            }

            items(filteredIssuingBanks, key = { "${it.code}${it.bankCode}" }) {
                BankItem(bank = it, onClick = {
                    onResult?.invoke(it)
                    if (popOnSelect) {
                        navController.setResult(it)
                        navController.popBackStack()
                    }
                })
            }
        }
    }
}

@Composable
private fun BankItem(bank: Bank, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = bank.name ?: "Unnamed Bank",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Divider(startIndent = 16.dp)
    }
}