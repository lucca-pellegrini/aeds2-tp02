import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Heapsort
{
    // Arquivo padrão contendo o CSV, se não receber por parâmetro.
    private static final String DEFAULT_DB = "/tmp/pokemon.csv";

    // Contém as estatísticas que o enunciado pede.
    private static final String LOG = "842986_heapsort.txt";
    private static final long MATRICULA = 842986;
    private static long tempoExecucao;

    public static void main(String[] args)
    {
        List<Pokemon> pokemon = new ArrayList<Pokemon>(801);

        // Stream do arquivo CSV.
        try (Scanner csvScanner =
                 new Scanner(new File((args.length > 0) ? args[0] : DEFAULT_DB))) {
            // Descarta a primeira linha (cabeçalho).
            csvScanner.nextLine();

            // Lê cada linha do CSV e cria um Pokémon.
            while (csvScanner.hasNextLine())
                pokemon.add(new Pokemon(csvScanner.nextLine()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Lê da entrada padrão.
        try (Scanner sc = new Scanner(System.in)) {
            String input;

            // Adiciona os Pokémon selecionados aos `selecionados`.
            List<Pokemon> selecionados = new ArrayList<Pokemon>();
            while (!(input = sc.nextLine()).equals("FIM"))
                selecionados.add(pokemon.get(Integer.parseInt(input) - 1));

            // Mede o tempo de execução na ordenação.
            long tempoInicial = System.nanoTime();
            heapsort(selecionados);
            tempoExecucao = System.nanoTime() - tempoInicial;

            // Imprime os Pokémon nos índices lidos.
            for (Pokemon p : selecionados)
                p.imprimir();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Salva o log.
        try (PrintStream log = new PrintStream(LOG)) {
            log.println(MATRICULA + "\t" + tempoExecucao + "\t" +
                        Pokemon.getNumComparacoes());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static <T extends Comparable<T> > void heapsort(List<T> vec)
    {
        // Primeiro, criamos uma lista clone com índice inicial 1.
        List<T> heap = new ArrayList<>(vec.size() + 1);

        heap.add(null); // Adicionamos um elemento nulo na posição 0.
        heap.addAll(vec); // Copiamos os elementos restantes.
        construir(heap); // Constrói o heap

        // Faz o heapsort.
        for (int i = heap.size() - 1; i > 1; --i) {
            swap(heap, 1, i); // Troca a raiz com a última folha.
            reconstruir(heap, 1, i - 1); // Reconstrói o heap.
        }

        // Copia os elementos do heap de volta para a lista original.
        for (int i = 0; i < vec.size(); i++)
            vec.set(i, heap.get(i + 1));
    }

    private static <T extends Comparable<T> > void construir(List<T> heap)
    {
        int n = heap.size() - 1;
        for (int i = n / 2; i >= 1; i--)
            reconstruir(heap, i, n);
    }

    private static <T extends Comparable<T> > void reconstruir(List<T> heap, int i, int n)
    {
        T temp = heap.get(i);
        int filho = i;

        while (2 * i <= n && filho == i) {
            filho = 2 * i;

            if (filho != n && heap.get(filho + 1).compareTo(heap.get(filho)) > 0)
                ++filho;

            if (temp.compareTo(heap.get(filho)) < 0) {
                heap.set(i, heap.get(filho));
                i = filho;
            } else {
                break;
            }
        }

        heap.set(i, temp);
    }

    private static <T> void swap(List<T> vec, int a, int b)
    {
        T temp = vec.get(a);
        vec.set(a, vec.get(b));
        vec.set(b, temp);
    }
}

class Pokemon implements Comparable<Pokemon>, Cloneable
{
    private int id, generation, captureRate;
    private String name, description;
    private List<PokeType> types;
    private List<String> abilities;
    private double weight, height;
    private boolean isLegendary;
    private LocalDate captureDate; // Os métodos de Date são deprecados.

    private static int numComparacoes = 0; // Para contar comparações.

    public Pokemon()
    {
        this.id = 0; // Chave padrão.
        this.generation = 0; // Geração padrão.
        this.name = "Desconhecido"; // Nome padrão.
        this.description = "Sem descrição"; // Descrição padrão.
        this.types = new ArrayList<>(); // Lista de tipos vazia.
        this.abilities = new ArrayList<>(); // Lista de habilidades vazia.
        this.weight = 0.0; // Peso padrão.
        this.height = 0.0; // Altura padrão.
        this.captureRate = 0; // Taxa de captura padrão.
        this.isLegendary = false; // Não é lendário por padrão.
        this.captureDate = LocalDate.MIN; // Data nula (01/01/-9999999…)
    }

    public Pokemon(int id, int generation, String name, String description,
                   List<PokeType> types, List<String> abilities, double weight,
                   double height, int captureRate, boolean isLegendary,
                   LocalDate captureDate)
    {
        this.id = id;
        this.generation = generation;
        this.name = name;
        this.description = description;
        this.types = types;
        this.abilities = abilities;
        this.weight = weight;
        this.height = height;
        this.captureRate = captureRate;
        this.isLegendary = isLegendary;
        this.captureDate = captureDate;
    }

    public Pokemon(String str)
    {
        this.ler(str);
    }

    public void ler(String str) throws ArrayIndexOutOfBoundsException
    {
        // Três seções principais da String de entrada: os elementos antes das
        // habilidades, a lista de habilidades em si, e os elementos após as
        // habilidades.
        String[] sec = str.split("\"");

        // Separa as seções em si em elementos individuais.
        String[] s1 = sec[0].split(",");
        String[] s2 = sec[2].split(",");

        // Lê os elementos iniciais.
        id = Integer.parseInt(s1[0]);
        generation = Integer.parseInt(s1[1]);
        name = s1[2];
        description = s1[3];

        // Adiciona os tipos. Usamos o .valueOf() do enum para facilitar.
        types = new ArrayList<>();
        types.add(PokeType.valueOf(s1[4].toUpperCase()));
        // Se tiver segundo tipo, adiciona-o.
        if (s1.length > 5 && !s1[5].isEmpty())
            types.add(PokeType.valueOf(s1[5].toUpperCase()));

        // Remove os caracteres extra da lista de abilidades e as adiciona.
        abilities = new ArrayList<>();
        for (String a : sec[1].split(", ")) {
            a = a.replace("[", "").replace("]", "").replace("'", "");
            if (!a.isEmpty())
                abilities.add(a);
        }

        // Adiciona peso e altura. Se estiverem vazios, devem ser 0.
        String weightStr = s2[1];
        String heightStr = s2[2];
        weight = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);
        height = heightStr.isEmpty() ? 0 : Double.parseDouble(heightStr);

        // Lê o determinante da probabilidade de captura e se é lendário ou não.
        captureRate = Integer.parseInt(s2[3]);
        isLegendary = (Integer.parseInt(s2[4]) == 1);

        // Adiciona data de captura.
        String[] membrosData = s2[5].split("/");
        captureDate = LocalDate.of(Integer.parseInt(membrosData[2]), // ano
                                   Integer.parseInt(membrosData[1]), // mês
                                   Integer.parseInt(membrosData[0])); // dia
    }

    public void imprimir()
    {
        System.out.println(this);
    }

    @Override public String toString()
    {
        String res = new String("[#");
        res +=
            id + " -> " + name + ": " + description + " - ['" +
            types.get(0).toString().toLowerCase() +
            ((types.size() == 2) ? "', '" + types.get(1).toString().toLowerCase() : "") +
            "'] - ['" + abilities.get(0) + "'";

        for (int i = 1; i < abilities.size(); ++i)
            res += ", '" + abilities.get(i) + "'";

        res += "] - " + weight + "kg - " + height + "m - " + captureRate + "% - " +
               isLegendary() + " - " + generation + " gen] - " +
               String.format("%02d/%02d/%04d", captureDate.getDayOfMonth(),
                             captureDate.getMonthValue(), captureDate.getYear());

        return res;
    }

    // Ordena Pokémon por altura.
    @Override public int compareTo(Pokemon outro)
    {
        ++Pokemon.numComparacoes;
        int res = Double.valueOf(this.height).compareTo(Double.valueOf(outro.height));
        res = res != 0 ? res : this.name.compareTo(outro.name);
        return res;
    }

    @Override public Pokemon clone()
    {
        try {
            Pokemon c = (Pokemon)super.clone();

            // Copia as listas, que são referências (Strings também são
            // referências, mas são imutáveis, então não é necessário).
            c.types = new ArrayList<>(this.types);
            c.abilities = new ArrayList<>(this.abilities);

            return c;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new AssertionError(); // Nunca deve acontecer.
        }
    }

    // Getters e Setters.

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getGeneration()
    {
        return generation;
    }

    public void setGeneration(int generation)
    {
        this.generation = generation;
    }

    public int getCaptureRate()
    {
        return captureRate;
    }

    public void setCaptureRate(int captureRate)
    {
        this.captureRate = captureRate;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<PokeType> getTypes()
    {
        return types;
    }

    public void setTypes(List<PokeType> types)
    {
        this.types = types;
    }

    public List<String> getAbilities()
    {
        return abilities;
    }

    public void setAbilities(List<String> abilities)
    {
        this.abilities = abilities;
    }

    public double getWeight()
    {
        return weight;
    }

    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public boolean isLegendary()
    {
        return isLegendary;
    }

    public void setLegendary(boolean isLegendary)
    {
        this.isLegendary = isLegendary;
    }

    public LocalDate getCaptureDate()
    {
        return captureDate;
    }

    public void setCaptureDate(LocalDate captureDate)
    {
        this.captureDate = captureDate;
    }

    public static int getNumComparacoes()
    {
        return numComparacoes;
    }

    public static void setNumComparacoes(int numComparacoes)
    {
        Pokemon.numComparacoes = numComparacoes;
    }

    // Tipos de Pokémon.
    static enum PokeType {
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
    }
}
