#define _POSIX_C_SOURCE 200809L

#include <ctype.h>
#include <math.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// De onde ler o CSV se não receber nenhum parâmetro na linha de comando.
#define DEFAULT_DB "/tmp/pokemon.csv"

/// Definições dos tipos de dados. ////////////////////////////////////////////

// Tipos possíveis de Pokémon.
enum PokeType {
	NO_TYPE = 0,
	BUG,
	DARK,
	DRAGON,
	ELECTRIC,
	FAIRY,
	FIGHTING,
	FIRE,
	FLYING,
	GHOST,
	GRASS,
	GROUND,
	ICE,
	NORMAL,
	POISON,
	PSYCHIC,
	ROCK,
	STEEL,
	WATER
};

// Definição do tipo de inteiro que armazena o tipo do Pokémon. Deve ter bits
// suficientes para todos os tipos.
typedef uint32_t PokeType;

// Lista de abilidades de um Pokémon.
typedef struct {
	char **list; // Lista dinâmica de strings dinâmicas.
	uint8_t num; // Quantidade de abilidades.
} PokeAbilities;

// Data.
typedef struct {
	uint16_t y; // Ano.
	uint8_t m; // Mês.
	uint8_t d; // Dia.
} Date;

// O Pokémon em si. Usamos tipos numéricos rígidos para economizar memória.
typedef struct {
	// Ordenamos os membros de maior (8 bytes) para menor (1 byte) para
	// melhorar o uso de memória, diminuindo o espaço vazio entre os
	// membros.

	// Tipos de 64 bits.
	double weight; // Peso em quilogramas.
	double height; // Altura em metros.

	// Ponteiros de 32 ou 64 bits, dependendo da máquina.
	char *name; // String dinâmica para o nome.
	char *description; // String dinâmica para a descrição.

	// Tipos de 32 bits.
	Date capture_date; // Data de captura.

	// Tipos de 16 bits.
	uint16_t id; // Chave: inteiro não-negativo de 16 bits.
	uint16_t capture_rate; // Determinante da probabilidade de captura.

	// Tipos de 8 bits.
	PokeType type1; // Primeiro tipo.
	PokeType type2; // Segundo tipo.
	uint8_t generation; // Geração: inteiro não-negativo de 8 bits.
	bool is_legendary; // Se é ou não um Pokémon lendário.

	// Tipo de tamamho irregular (72 bits) no final evita a introdução de
	// preenchimento no meio da struct.
	PokeAbilities abilities; // Lista dinâmica das abilidades.
} Pokemon;

/// Declarações de todas as funções. //////////////////////////////////////////

int main(int argc, char **argv);
void ler(Pokemon *restrict p, char *str);
static PokeAbilities abilities_from_string(char *str);
static PokeType type_from_string(const char *str);
static const char *type_to_string(PokeType type);

/// Métodos que operam nos nossos tipos de dados. /////////////////////////////

// Lê um Pokémon a partir de uma string. A string é modificada.
void ler(Pokemon *restrict p, char *str)
{
	char *tok; // Ponteiro temporário para as substrings (tokens).
	char *sav = NULL; // Ponteiro auxiliar para `strtok_r()`.

	// Lê a chave (id) e a geração.
	p->id = atoi(strtok_r(str, ",", &sav));
	p->generation = atoi(strtok_r(NULL, ",", &sav));

	// Lê o nome.
	tok = strtok_r(NULL, ",", &sav);
	p->name = malloc(strlen(tok) + 1);
	strcpy(p->name, tok);

	// Lê a descrição.
	tok = strtok_r(NULL, ",", &sav);
	p->description = malloc(strlen(tok) + 1);
	strcpy(p->description, tok);

	// Lê o primeiro tipo.
	p->type1 = type_from_string(strtok_r(NULL, ",", &sav));

	// Se existir um segundo tipo, adiciona-o.
	tok = strtok_r(NULL, ",", &sav);
	if (tok[0] != '"') { // Se o primeiro char não for `"`, há segundo tipo.
		p->type2 = type_from_string(tok);
		tok = strtok_r(NULL, ",", &sav); // Avança para o próximo token.
	} else {
		p->type2 = NO_TYPE;
	}

	// Lê as abilidades.
	tok = strtok_r(NULL, "\"", &sav);
	p->abilities = abilities_from_string(tok);

	// Leia peso e altura, se existirem (alguns Pokémon no CSV não têm, mas
	// todos que têm peso também têm altura, e vice-versa).
	if (*(tok + 1) != ',') { // Se o próximo char não for `,`, há peso.
		p->weight = atof(strtok_r(NULL, ",", &sav));
		p->height = atof(strtok_r(NULL, ",", &sav));
	} else {
		p->height = p-> weight = NAN; // Atribui um peso inválido.
	}

	// Lê o determinante da probabilidade de captura e se é lendário ou não.
	p->capture_rate = atoi(strtok_r(NULL, ",", &sav));
	p->is_legendary = atoi(strtok_r(NULL, ",", &sav));

	// Lê a data de captura.
	p->capture_date.d = atoi(strtok_r(NULL, "/", &sav));
	p->capture_date.m = atoi(strtok_r(NULL, "/", &sav));
	p->capture_date.y = atoi(strtok_r(NULL, "/", &sav));
}

// Cria uma lista dinâmica de abilidades a partir de uma representação textual.
static PokeAbilities abilities_from_string(char *str)
{
	PokeAbilities res = { .num = 1 }; // Há no mínimo uma habilidade.
	char *sav = NULL; // Ponteiro auxiliar para `strtok_r()`.

	// Conta o número de abilidades a partir das vírgulas na string.
	for (int i = 0; str[i] && str[i] != ']'; ++i)
		if (str[i] == ',')
			++res.num;

	// Aloca memória para a lista dinâmica.
	res.list = malloc(res.num * sizeof(char *));

	// Lê cada uma das habilidades.
	for (int i = 0; i < res.num; ++i) {
		char *ability; // Ponteiro temporário para a substring (token).
		int token_len = 0; // Contador to tamanho do token `ability`.

		// Extrai um token da lista.
		ability = strtok_r(str, ",]", &sav);
		str = NULL; // Para as chamadas subsequentes a `strtok_r()`.

		// Remove quaisquer caracteres exceto letras, números e espaços.
		for (int j = 0; ability[j]; ++j)
			if (isalnum(ability[j]) || isspace(ability[j]))
				ability[token_len++] = ability[j];
		ability[token_len] = '\0'; // Termina o token.

		// Remove espaços e aspas iniciais.
		while (*ability == ' ' || *ability == '\'') {
			++ability;
			--token_len;
		}

		// Remove espaços e aspas finais.
		while (token_len > 0 && (ability[token_len - 1] == ' ' ||
					 ability[token_len - 1] == '\''))
			ability[--token_len] = '\0';

		// Aloca a memória para a abilidade e a salva no struct.
		res.list[i] = malloc(token_len + 1);
		strcpy(res.list[i], ability);
	}

	return res;
}

// Converte a representação textual do tipo em um dado PokeType.
static PokeType type_from_string(const char *str)
{
	enum PokeType res;

	if (!strcmp(str, "bug")) {
		res = BUG;
	} else if (!strcmp(str, "dark")) {
		res = DARK;
	} else if (!strcmp(str, "dragon")) {
		res = DRAGON;
	} else if (!strcmp(str, "electric")) {
		res = ELECTRIC;
	} else if (!strcmp(str, "fairy")) {
		res = FAIRY;
	} else if (!strcmp(str, "fighting")) {
		res = FIGHTING;
	} else if (!strcmp(str, "fire")) {
		res = FIRE;
	} else if (!strcmp(str, "flying")) {
		res = FLYING;
	} else if (!strcmp(str, "ghost")) {
		res = GHOST;
	} else if (!strcmp(str, "grass")) {
		res = GRASS;
	} else if (!strcmp(str, "ground")) {
		res = GROUND;
	} else if (!strcmp(str, "ice")) {
		res = ICE;
	} else if (!strcmp(str, "normal")) {
		res = NORMAL;
	} else if (!strcmp(str, "poison")) {
		res = POISON;
	} else if (!strcmp(str, "psychic")) {
		res = PSYCHIC;
	} else if (!strcmp(str, "rock")) {
		res = ROCK;
	} else if (!strcmp(str, "steel")) {
		res = STEEL;
	} else if (!strcmp(str, "water")) {
		res = WATER;
	} else {
		fputs("FATAL: Pokémon tem um tipo desconhecido!\n", stderr);
		exit(EXIT_FAILURE);
	}

	return res;
}

// Converte um dado PokeType em sua representação textual.
static const char *type_to_string(PokeType type)
{
	const char *res = NULL;
	switch (type) {
	case BUG:
		res = "bug";
		break;
	case DARK:
		res = "dark";
		break;
	case DRAGON:
		res = "dragon";
		break;
	case ELECTRIC:
		res = "electric";
		break;
	case FAIRY:
		res = "fairy";
		break;
	case FIGHTING:
		res = "fighting";
		break;
	case FIRE:
		res = "fire";
		break;
	case FLYING:
		res = "flying";
		break;
	case GHOST:
		res = "ghost";
		break;
	case GRASS:
		res = "grass";
		break;
	case GROUND:
		res = "ground";
		break;
	case ICE:
		res = "ice";
		break;
	case NORMAL:
		res = "normal";
		break;
	case POISON:
		res = "poison";
		break;
	case PSYCHIC:
		res = "psychic";
		break;
	case ROCK:
		res = "rock";
		break;
	case STEEL:
		res = "steel";
		break;
	case WATER:
		res = "water";
		break;
	default:
		res = "unknown";
		break;
	}

	return res;
}

/// Programa principal. ///////////////////////////////////////////////////////

int main(int argc, char **argv)
{
	(void)argc, (void)argv;
	return EXIT_SUCCESS;
}
