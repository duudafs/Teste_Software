import { test, expect } from '@playwright/test';

// Regras da página (/frete):
// - CEP deve ter exatamente 8 dígitos numéricos.
// - CEP iniciado por "8": frete de R$ 15,00; demais CEPs válidos: R$ 25,00.
// - Valor deve ser numérico positivo, com no máximo 2 casas decimais
//   (separador "," ou ".").
// - Pedidos com valor >= R$ 200,00 têm frete grátis, independente do CEP.

test.describe('calculadora de frete — caminhos válidos e valores-limite', () => {
  const casosValidos = [
    { cep: '80000000', valor: '100', classe: 'CEP inicia com 8, valor inteiro abaixo do frete grátis', esperado: 'Frete: R$ 15,00' },
    { cep: '01000000', valor: '100', classe: 'CEP não inicia com 8, valor inteiro abaixo do frete grátis', esperado: 'Frete: R$ 25,00' },
    { cep: '01000000', valor: '150,75', classe: 'valor com separador decimal vírgula', esperado: 'Frete: R$ 25,00' },
    { cep: '01000000', valor: '150.75', classe: 'valor com separador decimal ponto', esperado: 'Frete: R$ 25,00' },
    { cep: '01000000', valor: '199,99', classe: 'valor no limite inferior — logo abaixo do frete grátis', esperado: 'Frete: R$ 25,00' },
    { cep: '01000000', valor: '200', classe: 'valor no limite exato do frete grátis (R$ 200,00)', esperado: 'Frete grátis' },
    { cep: '01000000', valor: '200,01', classe: 'valor no limite superior — logo acima do frete grátis', esperado: 'Frete grátis' },
    { cep: '80000000', valor: '250', classe: 'frete grátis prevalece mesmo com CEP iniciado por 8', esperado: 'Frete grátis' },
  ];

  for (const caso of casosValidos) {
    test(`CEP ${caso.cep} / valor ${caso.valor} — ${caso.classe}`, async ({ page }) => {
      await page.goto('/frete');
      await page.getByLabel('CEP').fill(caso.cep);
      await page.getByLabel('Valor do pedido').fill(caso.valor);
      await page.getByRole('button', { name: 'Calcular frete' }).click();

      const resultado = page.locator('#resultado');
      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText(caso.esperado);
      await expect(resultado).toHaveAttribute('role', 'status');
    });
  }
});

test.describe('calculadora de frete — classes inválidas', () => {
  const casosInvalidos = [
    { cep: '8000000', valor: '100', classe: 'CEP com 7 dígitos (abaixo do tamanho mínimo)' },
    { cep: '800000000', valor: '100', classe: 'CEP com 9 dígitos (acima do tamanho máximo)' },
    { cep: '8000000A', valor: '100', classe: 'CEP com caractere não numérico' },
    { cep: '', valor: '100', classe: 'CEP vazio' },
    { cep: '01000000', valor: '0', classe: 'valor igual a zero (limite inferior inválido)' },
    { cep: '01000000', valor: '-10', classe: 'valor negativo' },
    { cep: '01000000', valor: 'abc', classe: 'valor não numérico' },
    { cep: '01000000', valor: '10,999', classe: 'valor com 3 casas decimais' },
    { cep: '01000000', valor: '', classe: 'valor vazio' },
  ];

  for (const caso of casosInvalidos) {
    test(`CEP "${caso.cep}" / valor "${caso.valor}" — ${caso.classe}`, async ({ page }) => {
      await page.goto('/frete');
      await page.getByLabel('CEP').fill(caso.cep);
      await page.getByLabel('Valor do pedido').fill(caso.valor);
      await page.getByRole('button', { name: 'Calcular frete' }).click();

      const resultado = page.locator('#resultado');
      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText('Dados inválidos');
      await expect(resultado).toHaveAttribute('role', 'alert');
    });
  }
});
