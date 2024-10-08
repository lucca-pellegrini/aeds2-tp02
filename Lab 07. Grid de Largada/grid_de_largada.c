#include <limits.h>
#include <stdio.h>
#include <stdlib.h>

int main(void)
{
	int *largada = NULL, *chegada = NULL; // Arrays dos competidores.
	int *mapa = NULL; // Array com as posições de chegada.
	int num = 0, tam = 0; // Dimensões dos arrays.

	// Lê número de competidores enquanto houver inteiros na input.
	while (scanf("%d", &num) != EOF) {
		int ultrapassagens = 0; // Contador de ultrapassagens.

		// (Re)aloca memória para os arrays, se necessário.
		if (num > tam) {
			largada = realloc(largada, sizeof(int[tam = num]));
			chegada = realloc(chegada, sizeof(int[tam]));
			// O primeiro competidor é 1, não 0. Logo, aloca-se
			// para `tam + 1`.
			mapa = realloc(mapa, sizeof(int[tam + 1]));
		}

		// Lê os competidores na ordem de largada e chegada.
		for (int i = 0; i < num; ++i)
			scanf("%d", largada + i);

		for (int i = 0; i < num; ++i) {
			scanf("%d", chegada + i);
			mapa[chegada[i]] = i; // Constrói o mapa de posições.
		}

		// Itera sobre cada competidor e, para cada competidor
		// subsequente, verifica se sua ordem relativa é diferente no
		// array `chegada` (usamos aqui o mapa, pois a complexidade de
		// acesso é constante). Caso for, sabemos que o competidor em
		// `i` ultrapassou (ou foi ultrapassado pelo) competidor em
		// `j`. Como estamos iterando sobre cada competidor em dois
		// laços aninhados, sabemos que o número de comparações tem
		// complexidade de Θ(n²).
		for (int i = 0; i < num; ++i)
			for (int j = i + 1; j < num; ++j)
				if (mapa[largada[i]] > mapa[largada[j]])
					++ultrapassagens;

		printf("%d\n", ultrapassagens);
	}

	free(largada);
	free(chegada);
	free(mapa);
	return EXIT_SUCCESS;
}
