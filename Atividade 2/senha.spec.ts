import { test, expect } from '@playwright/test';

// Regras da página (/senha):
// - Senha deve ter de 8 a 20 caracteres.
// - Deve conter ao menos uma letra maiúscula, uma minúscula e um número.
// - Não pode conter espaços.
// - Confirmação deve ser idêntica à senha.

test.describe('cadastro de senha — caminhos válidos, classes inválidas e valores-limite', () => {
  const casos = [
    { senha: 'Abcdefg1', classe: 'limite mínimo de tamanho (8 caracteres)', valido: true },
    { senha: 'Abcdef1', classe: 'abaixo do tamanho mínimo (7 caracteres)', valido: false },
    { senha: 'Abcdefghij12', classe: 'tamanho dentro da faixa válida (12 caracteres)', valido: true },
    { senha: 'Abcdefghijklmnopqr12', classe: 'limite máximo de tamanho (20 caracteres)', valido: true },
    { senha: 'Abcdefghijklmnopqr123', classe: 'acima do tamanho máximo (21 caracteres)', valido: false },
    { senha: 'abcdefg1', classe: 'sem letra maiúscula', valido: false },
    { senha: 'ABCDEFG1', classe: 'sem letra minúscula', valido: false },
    { senha: 'Abcdefgh', classe: 'sem número', valido: false },
    { senha: 'Abcdef 1', classe: 'contém espaço', valido: false },
    { senha: '', classe: 'vazio', valido: false },
  ];

  for (const caso of casos) {
    test(`senha "${caso.senha || '(vazia)'}" — ${caso.classe}`, async ({ page }) => {
      await page.goto('/senha');
      await page.getByLabel('Nova senha').fill(caso.senha);
      await page.getByLabel('Confirmar senha').fill(caso.senha);
      await page.getByRole('button', { name: 'Cadastrar senha' }).click();

      const resultado = page.locator('#resultado');
      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText(caso.valido ? 'Senha cadastrada' : 'Senha fora do padrão');
      await expect(resultado).toHaveAttribute('role', caso.valido ? 'status' : 'alert');
    });
  }

  test('nega cadastro quando a confirmação não coincide com a senha', async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill('Abcdefg1');
    await page.getByLabel('Confirmar senha').fill('Abcdefg2');
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toBeVisible();
    await expect(resultado).toHaveText('As senhas não coincidem');
    await expect(resultado).toHaveAttribute('role', 'alert');
  });

  test('limpa o formulário após cadastro bem-sucedido', async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill('Abcdefg1');
    await page.getByLabel('Confirmar senha').fill('Abcdefg1');
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('Senha cadastrada');
    await expect(page.getByLabel('Nova senha')).toHaveValue('');
    await expect(page.getByLabel('Confirmar senha')).toHaveValue('');
  });
});
