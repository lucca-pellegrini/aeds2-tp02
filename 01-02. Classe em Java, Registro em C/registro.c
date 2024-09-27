#define _POSIX_C_SOURCE 200809L

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// De onde ler o CSV se não receber nenhum parâmetro na linha de comando.
#define DEFAULT_DB "/tmp/pokemon.csv"

/// Definições dos tipos de dados. ////////////////////////////////////////////

// Tipos possíveis de Pokémon. Usamos bitmask para economizar memória.
enum PokeType {
	BUG = 1 << 0,
	DARK = 1 << 1,
	DRAGON = 1 << 2,
	ELECTRIC = 1 << 3,
	FAIRY = 1 << 4,
	FIGHTING = 1 << 5,
	FIRE = 1 << 6,
	FLYING = 1 << 7,
	GHOST = 1 << 8,
	GRASS = 1 << 9,
	GROUND = 1 << 10,
	ICE = 1 << 11,
	NORMAL = 1 << 12,
	POISON = 1 << 13,
	PSYCHIC = 1 << 14,
	ROCK = 1 << 15,
	STEEL = 1 << 16,
	WATER = 1 << 17
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
	PokeType types; // Bitmask dos tipos.
	Date capture_date; // Data de captura.

	// Tipos de 16 bits.
	uint16_t id; // Chave: inteiro não-negativo de 16 bits.
	uint16_t capture_rate; // Determinante da probabilidade de captura.

	// Tipos de 8 bits.
	uint8_t generation; // Geração: inteiro não-negativo de 8 bits.
	bool is_legendary; // Se é ou não um Pokémon lendário.

	// Tipo de tamamho irregular (72 bits) no final evita a introdução de
	// preenchimento no meio da struct.
	PokeAbilities abilities; // Lista dinâmica das abilidades.
} Pokemon;

/// Declarações de todas as funções. //////////////////////////////////////////

int main(int argc, char **argv);
void ler(Pokemon *restrict p, char *str);
static PokeAbilities abilities_from_string(const char *str);
static uint8_t get_type_count(PokeType mask);
static PokeType type_from_string(const char *str);
static const char *type_to_string(PokeType type);

/// Métodos que operam nos nossos tipos de dados. /////////////////////////////

// Lê um Pokémon a partir de uma string. A string é modificada.
void ler(Pokemon *restrict p, char *str)
{
	char *tmp; // Ponteiro temporário para as substrings.
	char *sav; // Ponteiro auxiliar para `strtok_r()`.

	// Lê a chave (id) e a geração.
	p->id = atoi(strtok_r(str, ",", &sav));
	p->generation = atoi(strtok_r(NULL, ",", &sav));

	// Lê o nome.
	tmp = strtok_r(NULL, ",", &sav);
	p->name = malloc(strlen(tmp) + 1);
	strcpy(p->name, tmp);

	// Lê a descrição.
	tmp = strtok_r(NULL, ",", &sav);
	p->description = malloc(strlen(tmp) + 1);
	strcpy(p->description, tmp);

	// Lê o primeiro tipo.
	p->types = type_from_string(strtok_r(NULL, ",", &sav));

	// Se existir um segundo tipo, adiciona-o à bitmask `types`.
	tmp = strtok_r(NULL, ",", &sav);
	if (tmp[0] != '"') { // Se o primeiro char não for `"`, há segundo tipo.
		p->types |= type_from_string(tmp);
		tmp = strtok_r(NULL, ",", &sav); // Avança para o próximo token.
	}

	// Lê as abilidades.
	p->abilities = abilities_from_string(strtok_r(NULL, "]", &sav));
}

// Determina quantos tipos o Pokemon tem: 0, 1, ou 2, a partir da bitmask.
static uint8_t get_type_count(PokeType mask)
{
	uint8_t res;
	if (mask <= 0)
		res = 0;
	else if ((mask & (mask - 1)) == 0) // Verifica se é potência de 2.
		res = 1;
	else
		res = 2;
	return res;
}

// Cria uma lista dinâmica de abilidades a partir de uma representação textual.
static PokeAbilities abilities_from_string(const char *str)
{
	PokeAbilities res = { .num = 1 }; // Há no mínimo uma habilidade.
	char *tmp; // Ponteiro temporário para as substrings.
	char *sav; // Ponteiro auxiliar para `strtok_r()`.

	// Conta o número de abilidades a partir das vírgulas na string.
	for (int i = 0; str[i] != ']'; ++i)
		if (str[i] == ',')
			++res.num;

	res.list = malloc(res.num);

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
