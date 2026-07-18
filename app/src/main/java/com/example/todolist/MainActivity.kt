package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ui.theme.TodoLIstTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoLIstTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoAppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Modelo de dados: cada tarefa tem um id único e um estado de concluída
data class Tarefa(
    val id: String = UUID.randomUUID().toString(),
    val texto: String,
    val concluida: Boolean = false
)

/**
 * Controla a navegação entre a Tela 1 (Lista) e a Tela 2 (Detalhes).
 * O estado da lista de tarefas vive aqui, no nível mais alto, e é
 * compartilhado entre as telas — assim ambas enxergam a mesma informação atualizada.
 */
@Composable
fun TodoAppNavigation(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    var listaDeTarefas by remember { mutableStateOf(listOf<Tarefa>()) }

    fun alternarConcluida(id: String) {
        listaDeTarefas = listaDeTarefas.map {
            if (it.id == id) it.copy(concluida = !it.concluida) else it
        }
    }

    fun removerTarefa(id: String) {
        listaDeTarefas = listaDeTarefas.filterNot { it.id == id }
    }

    NavHost(
        navController = navController,
        startDestination = "lista",
        modifier = modifier
    ) {
        // Rota da Tela 1
        composable("lista") {
            TodoListScreen(
                listaDeTarefas = listaDeTarefas,
                onAdicionarTarefa = { texto ->
                    listaDeTarefas = listaDeTarefas + Tarefa(texto = texto)
                },
                onTarefaClick = { tarefa ->
                    // Navega para a Tela 2, passando o id da tarefa selecionada
                    navController.navigate("detalhes/${tarefa.id}")
                },
                onCheckboxClick = { id -> alternarConcluida(id) },
                onDeleteClick = { id -> removerTarefa(id) }
            )
        }

        // Rota da Tela 2 — recebe o tarefaId como argumento de navegação
        composable("detalhes/{tarefaId}") { backStackEntry ->
            val tarefaId = backStackEntry.arguments?.getString("tarefaId")
            val tarefaSelecionada = listaDeTarefas.find { it.id == tarefaId }

            TarefaDetalhesScreen(
                tarefa = tarefaSelecionada,
                onVoltarClick = { navController.popBackStack() },
                onConcluidaToggle = { id -> alternarConcluida(id) }
            )
        }
    }
}

// ======================= TELA 1: LISTA DE TAREFAS =======================

@Composable
fun TodoListScreen(
    listaDeTarefas: List<Tarefa>,
    onAdicionarTarefa: (String) -> Unit,
    onTarefaClick: (Tarefa) -> Unit,
    onCheckboxClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tarefaAtual by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {

        Text(
            text = "Minhas Tarefas",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = tarefaAtual,
                onValueChange = { tarefaAtual = it },
                label = { Text("Nova tarefa") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                if (tarefaAtual.isNotBlank()) {
                    onAdicionarTarefa(tarefaAtual.trim())
                    tarefaAtual = ""
                }
            }) {
                Text("Adicionar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (listaDeTarefas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma tarefa ainda. Adicione uma acima!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val listaOrdenada = listaDeTarefas.sortedBy { it.concluida }

            LazyColumn {
                items(listaOrdenada, key = { it.id }) { tarefa ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onTarefaClick(tarefa) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tarefa.concluida,
                                onCheckedChange = { onCheckboxClick(tarefa.id) }
                            )

                            Text(
                                text = tarefa.texto,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 16.dp),
                                textDecoration = if (tarefa.concluida)
                                    TextDecoration.LineThrough else TextDecoration.None,
                                color = if (tarefa.concluida)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = { onDeleteClick(tarefa.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "Excluir tarefa"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================= TELA 2: DETALHES DA TAREFA =======================

@Composable
fun TarefaDetalhesScreen(
    tarefa: Tarefa?,
    onVoltarClick: () -> Unit,
    onConcluidaToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botão voltar no topo
        TextButton(onClick = onVoltarClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Voltar")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (tarefa == null) {
            // Caso a tarefa não seja encontrada (ex: foi excluída)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tarefa não encontrada.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        text = "Título da Tarefa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tarefa.texto,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (tarefa.concluida)
                                Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (tarefa.concluida)
                                Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (tarefa.concluida) "Concluída" else "Pendente",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onConcluidaToggle(tarefa.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (tarefa.concluida) "Marcar como pendente" else "Marcar como concluída")
            }
        }
    }
}