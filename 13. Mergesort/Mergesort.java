import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mergesort
{
    // Arquivo padrão contendo o CSV, se não receber por parâmetro.
    private static final String DEFAULT_DB = "/tmp/pokemon.csv";

    // Contém as estatísticas que o enunciado pede.
    private static final String LOG = "842986_mergesort.txt";
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

            // Libera os Pokémon não selecionados.
            pokemon = null;

            // Mede o tempo de execução na ordenação.
            long tempoInicial = System.nanoTime();
            mergesort(selecionados);
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

    public static <T extends Comparable<T> > void mergesort(List<T> vec)
    {
        if (vec.size() > 1) {
            int mid = vec.size() / 2; // Índice médio.
            List<T> esq = new ArrayList<>(mid + 1);
            List<T> dir = new ArrayList<>(mid + 1);

            // Copia cada metade da lista.
            for (int i = 0; i < mid; ++i)
                esq.add(vec.get(i));
            for (int i = mid; i < vec.size(); ++i)
                dir.add(vec.get(i));

            // Invoca recursivamente nas metades.
            mergesort(esq);
            mergesort(dir);

            // Entrelaça os resultados em ordem.
            merge(vec, esq, dir);
        }
    }

    private static <T extends Comparable<T> > void merge(List<T> vec, List<T> esq,
                                                         List<T> dir)
    {
        int esqIdx = 0, dirIdx = 0, vecIdx = 0; // Índices de cada lista.

        // Itera sobre os elementos um a um, entrelaçando-os em ordem.
        while (esqIdx < esq.size() && dirIdx < dir.size()) {
            if (esq.get(esqIdx).compareTo(dir.get(dirIdx)) <= 0)
                vec.set(vecIdx++, esq.get(esqIdx++));
            else
                vec.set(vecIdx++, dir.get(dirIdx++));
        }

        // Adiciona os elementos que sobraram.
        while (esqIdx < esq.size())
            vec.set(vecIdx++, esq.get(esqIdx++));
        while (dirIdx < dir.size())
            vec.set(vecIdx++, dir.get(dirIdx++));
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
        int res = this.types.get(0).toString().compareTo(outro.types.get(0).toString());
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
