import java.awt.print.Printable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

public class Classe {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}

class Pokemon implements /* Comparable<Pokemon>, */ Cloneable {
    int id, generation, captureRate;
    String name, description;
    ArrayList<PokeType> types;
    ArrayList<String> abilities;
    double weight, height;
    boolean isLegendary;
    LocalDate captureDate;

    public Pokemon() {
    }

    public Pokemon(String str) {
        this.ler(str);
    }

    public void ler(String str) throws ArrayIndexOutOfBoundsException {
        // Três seções principais da String de entrada: os elementos antes das
        // habilidades, a lista de habilidades em si, e os elementos após as
        // habilidades.
        String[] sec = str.split("\"");

        // Separa as seções em si em elementos individuais.
        String[] s1 = sec[0].split(",");
        String[] s2 = sec[2].split(",");

        // Cria um array de tamanho fixo para armazenar todos os membros.
        String[] members = new String[s1.length + s2.length];

        // Copia os elementos para o array único
        int i = 0;
        for (String element : s1)
            members[i++] = element;
        for (String element : s2)
            members[i++] = element;

        // Lê os elementos iniciais.
        id = Integer.parseInt(members[0]);
        generation = Integer.parseInt(members[1]);
        name = members[2];
        description = members[3];

        // Adiciona os tipos. Usamos o .valueOf() do enum para facilitar.
        types = new ArrayList<>();
        types.add(PokeType.valueOf(members[4].toUpperCase()));
        if (!members[5].isEmpty()) // Se tiver segundo tipo, adiciona-o.
            types.add(PokeType.valueOf(members[5].toUpperCase()));

        // Remove os caracteres extra da lista de abilidades e as adiciona.
        abilities = new ArrayList<>();
        for (String a : sec[1].split(", "))
            abilities.add(a.replace("[", "").replace("]", "").replace("'", ""));

        // Adiciona peso e altura. Se estiverem vazios, devem ser 0.
        String weightStr = members[6];
        String heightStr = members[7];
        weight = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);
        height = heightStr.isEmpty() ? 0 : Double.parseDouble(heightStr);

        // Lê o determinante da probabilidade de captura e se é lendário ou não.
        captureRate = Integer.parseInt(members[8]);
        isLegendary = (Integer.parseInt(members[9]) == 1);

        // Adiciona data de captura.
        String[] membrosData = members[10].split("/");
        captureDate = LocalDate.of(Integer.parseInt(membrosData[2]), // ano
                Integer.parseInt(membrosData[1]), // mês
                Integer.parseInt(membrosData[0])); // dia
    }

    public void imprimir() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        String res = new String("[#");
        res += id + " -> " + name + ": " + description + " - ['" + types.get(0) +
                ((types.size() == 2) ? types.get(1) : "") + "] - [ '"
                + "'";

        for (String a : abilities)
            res += ", '" + a + "'";

        res += "] - " + weight + "kg - " + height + "m - " + captureRate + "% - " +
                generation + "gen] - " + captureDate.getDayOfMonth() + '/' +
                captureDate.getMonthValue() + '/' + captureDate.getYear();

        return res;
    }

    @Override
    public Pokemon clone() {
        Pokemon c = (Pokemon) super.clone();

        // Copia as listas, que são referências (Strings também são
        // referências, mas são imutáveis, então não é necessário).
        c.types = new ArrayList<>(this.types);
        c.abilities = new ArrayList<>(this.abilities);

        return c;
    }

    // Getters e Setters.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getCaptureRate() {
        return captureRate;
    }

    public void setCaptureRate(int captureRate) {
        this.captureRate = captureRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList getTypes() {
        return types;
    }

    public void setTypes(ArrayList types) {
        this.types = types;
    }

    public ArrayList getAbilities() {
        return abilities;
    }

    public void setAbilities(ArrayList abilities) {
        this.abilities = abilities;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isLegendary() {
        return isLegendary;
    }

    public void setLegendary(boolean isLegendary) {
        this.isLegendary = isLegendary;
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
