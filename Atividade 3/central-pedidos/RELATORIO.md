# Relatório do grupo

Integrantes: João Paulo

## 1. Grafo de chamadas de `PedidoService.fechar`

```
fechar(Pedido, Cliente)
  -> Cliente.bloqueado()
  -> Pedido.subtotalCentavos()
  -> Pedido.estoqueSuficiente() -> ItemPedido.disponivel()
  -> PoliticaDesconto.calcular(Cliente, long, String)
  -> CalculadoraFrete.calcular(Pedido, Cliente, long) -> Pedido.pesoGramas() / Pedido.temFragil()
  -> AnaliseRisco.avaliar(Cliente, long, boolean)
  -> PagamentoService.pagar(long, int) -> ProcessadorPagamento.autorizar(long)
```

`fechar` chama as classes colaboradoras nesta ordem fixa: bloqueio → subtotal → estoque → desconto → frete → risco → pagamento. Cada retorno antecipado (`BLOQUEADO`, `SEM_ESTOQUE`, `REVISAO`) interrompe a cadeia antes das etapas seguintes.

## 2. CFGs e complexidade ciclomática

Convenção adotada: cada `&&`/`||` é modelado como um nó de decisão próprio (avaliação de curto-circuito), cada `switch` conta como decisões `case − 1` mais a condição de entrada, e cada método tem saída unificada em um nó `fim` para o cálculo de `V(G) = E − N + 2`.

### 2.1 `PoliticaDesconto.calcular`

Nós: 1 entrada, 1 decisão (`subtotal<0`), 1 decisão (`vip`), 1 decisão (`subtotal>=50000`), 1 decisão (`cupom nulo/branco`), 1 switch com 2 cases + default (2 decisões), 2 decisões internas dos cases (`comprasAnteriores==0 && subtotal>=10000` e `subtotal>=20000`), 1 decisão do teto (ternário), 1 saída unificada.

- N = 12, E = 15 → V(G) = 15 − 12 + 2 = **5** decisões relevantes → nesse método a complexidade calculada pelas decisões simples do JaCoCo é `9` branches (a condição composta do `BEMVINDO` conta como 2 decisões de curto-circuito).

### 2.2 `CalculadoraFrete.calcular`

Nós: entrada, decisão `liquido<0`, switch UF (3 saídas → 2 decisões), `while` (1 decisão, laço), decisão `liquido>=30000 && !expresso` (curto-circuito, 2 nós), decisão `vip`, decisão `expresso`, decisão `temFragil`, saída.

- Decisões simples (branches): 1 (liquido negativo) + 2 (switch) + 1 (while) + 2 (curto-circuito do zeramento) + 1 (vip) + 1 (expresso) + 1 (fragil) = **9**, condizente com `BRANCH_COVERED=17` do JaCoCo (17 branches reais = 8 pontos de decisão × 2 saídas, mais o switch de 3 ramos).

### 2.3 `AnaliseRisco.avaliar`

Decisões: `total<0`, `bloqueado`, `comprasAnteriores==0`, `total>100000 || expresso` (curto-circuito, 2 nós), `total>500000 && !vip` (curto-circuito, 2 nós).

- V(G) = decisões + 1 = 1+1+1+2+2 + 1 = **8**, compatível com `BRANCH_COVERED=14` (7 pontos de decisão binários).

### 2.4 `PagamentoService.pagar`

Decisões: `total<=0`, `maxTentativas<1 || >3` (curto-circuito, 2 nós), `do/while` (1 decisão), `try/catch` (o catch não é contado como branch pelo JaCoCo, mas é um nó de desvio real no CFG).

- V(G) modelado = decisões + 1 = 1+2+1 + 1 = **5** no CFG completo; o JaCoCo reporta `BRANCH_COVERED=8` (4 decisões binárias observáveis, sem contar o desvio de exceção).

### 2.5 `PedidoService.fechar`

Decisões: `bloqueado`, `subtotal==0`, `!estoqueSuficiente`, `!analise.equals("APROVADO")`, resultado do pagamento (`? :`).

- V(G) = 5 decisões + 1 = **6**, compatível com `BRANCH_COVERED=10` do JaCoCo (5 pontos binários).

## 3. Base de caminhos independentes (exemplos com dados)

| Método | Caminho | Dado que exercita |
| --- | --- | --- |
| `AnaliseRisco.avaliar` | bloqueado | `Cliente(false,true,0)`, total=1 |
| `AnaliseRisco.avaliar` | novo + total alto | `Cliente(false,false,0)`, total=100_001 |
| `AnaliseRisco.avaliar` | novo + expresso | `Cliente(false,false,0)`, total=1, expresso=true |
| `AnaliseRisco.avaliar` | novo aprovado | `Cliente(false,false,0)`, total=100_000, expresso=false |
| `AnaliseRisco.avaliar` | histórico + alto + não vip | `Cliente(false,false,3)`, total=500_001 |
| `AnaliseRisco.avaliar` | histórico + alto + vip | `Cliente(true,false,3)`, total=500_001 |
| `AnaliseRisco.avaliar` | histórico aprovado | `Cliente(false,false,3)`, total=500_000 |
| `PedidoService.fechar` | bloqueado | cliente bloqueado |
| `PedidoService.fechar` | subtotal zero | item com quantidade 0 |
| `PedidoService.fechar` | sem estoque | quantidade > estoque |
| `PedidoService.fechar` | revisão | cliente novo, total alto |
| `PedidoService.fechar` | pago | fluxo completo aprovado |
| `PedidoService.fechar` | pagamento recusado | processador retorna `false` |

O caminho "cliente `bloqueado` **e** `total>500000` sem VIP" em `AnaliseRisco` é viável isoladamente na unidade (chamando `avaliar` direto), mas **inviável via `PedidoService`**: o serviço já retorna `BLOQUEADO` antes de calcular subtotal, desconto, frete e total — `avaliar` nunca é chamado para cliente bloqueado através do serviço.

## 4. Matriz de testes × caminhos (resumo)

| Classe de teste | Testes | Caminhos/ramos cobertos |
| --- | --- | --- |
| `ClienteTest` | 1 | construção válida, histórico negativo |
| `ItemPedidoTest` | 4 | limites de preço, quantidade, estoque e peso; disponibilidade |
| `PedidoTest` | 4 | lista nula/vazia/limite, UF válida/inválida, itens ativos/inativos, fragilidade, estoque |
| `PoliticaDescontoTest` | 4 | VIP, comum acima/abaixo do limiar, cupons BEMVINDO/EXTRA10/desconhecido, teto de 20% |
| `CalculadoraFreteTest` | 4 | UFs, laço de peso (0/1/2 iterações, fração), zeramento, VIP, expresso, frágil |
| `AnaliseRiscoTest` | 3 | bloqueado, novo (alto/expresso/aprovado), histórico (alto vip/não vip/aprovado) |
| `PagamentoServiceTest` | 4 | validações, aprovação imediata, recusa sem repetição, retomada após indisponibilidade, esgotamento, exceção não tratada |
| `PedidoServiceTest` | 4 | BLOQUEADO, subtotal zero, SEM_ESTOQUE, REVISAO, PAGO, PAGAMENTO_RECUSADO, nulos |

Total: **28 testes**, todos verdes. Cada método de teste agrupa várias asserções/caminhos relacionados (ex.: `deveValidarSkuPrecoEEstoqueInvalidos`, `bloqueadoESubtotalZeroInterrompemAntesDeQualquerCobranca`) em vez de um `@Test` por combinação isolada, mantendo 100% de cobertura com poucos métodos.

## 5. Evolução da cobertura

| Etapa | Testes executados | Linhas | Branches | Métodos | Classes | Lacunas |
| --- | --- | --- | --- | --- | --- | --- |
| Inicial (só exemplo) | 1 | baixa (só `fechar` caminho feliz) | baixa | poucas | poucas | quase tudo descoberto |
| Final | 28 | 100% | 100% | 100% | 100% | nenhuma linha/branch alcançável restante (ver `target/site/jacoco/jacoco.csv`) |

## 6. Mutação de verificação

Alterada temporariamente a regra de desconto VIP em `PoliticaDesconto` (`subtotal * 10 / 100` → `subtotal * 15 / 100`). Resultado: `mvn test` falhou em `PoliticaDescontoTest.deveAplicarDescontoBaseConformeTipoDeClienteESemCupom` (esperado 1000, obtido 1500). A alteração foi desfeita e a suíte voltou a passar (28/28).

## 7. Análise crítica

- **Cobertura de ramos não é cobertura de caminhos:** em `CalculadoraFrete`, o `while` de peso excedente foi coberto com 0, 1 e 2 iterações, mas o JaCoCo só registra se a condição do laço foi avaliada como verdadeira e falsa — não distingue "1 iteração" de "5 iterações". Dois testes que cobrem o mesmo branch podem exercitar caminhos completos diferentes.
- **Curto-circuito:** em `AnaliseRisco`, a condição `total>100_000 || expresso` tem o operando direito (`expresso`) só avaliado quando o esquerdo é falso. Dentro de `clienteNovoVaiParaRevisaoPorTotalOuPorExpresso`, o caso com total baixo e expresso=true garante que o segundo operando realmente é avaliado, evitando falso-positivo de cobertura.
- **Caminho inviável via serviço:** como citado na seção 3, `AnaliseRisco` tem uma combinação (`bloqueado` com total alto) inalcançável a partir de `PedidoService.fechar`, pois o retorno antecipado de bloqueio intercepta antes.
- **Exceção não contada como branch:** o `catch (IllegalStateException)` em `PagamentoService.pagar` não aparece como ramo no relatório do JaCoCo, mas é um desvio real de fluxo. Os testes `deveRepetirAposIndisponibilidadeAteAprovarOuEsgotarTentativas` e `devePropagarOutrasExcecoesSemRepetir` cobrem esse comportamento mesmo sem contar para a métrica de branch.
- **Mutação:** a alteração proposital na taxa VIP foi detectada por `PoliticaDescontoTest.deveAplicarDescontoBaseConformeTipoDeClienteESemCupom`, confirmando que a suíte tem assert de valor (não só de ausência de exceção) sobre essa regra. A alteração foi revertida antes da entrega.
