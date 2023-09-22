import kotlin.math.max

fun main() {
    val player = Player(2, 1, 30, 5..10)
    val monsters = List(2) { Monster(2, 1, 15, 1..6) }
    val monsterNumbers = 1..monsters.size
    val possibleAttacks = monsterNumbers.asSequence().map { "$it" }.toSet()
    println("Вводите номер монстра из интервала $monsterNumbers, чтобы атаковать его, 'h' чтобы лечиться")
    while (player.isAlive && monsters.any(Monster::isAlive)) {
        player.run { println("Игрок:      $health/$maxHealth здоровья, $damage повреждений, доступно $remainingHeals исцелений") }
        println(
            """
            Монстры: ${
                monsters.withIndex()
                    .joinToString(" | ") { (index, monster) -> monster.run { "№${index + 1} $health/$maxHealth здоровья, $damage повреждений" } }
            }
            
            """.trimIndent()
        )
        val action = generateSequence(readln()) {
            println("Неверный ввод '$it'. Попробуйте ещё раз")
            readln()
        }.first { it in possibleAttacks + "h" }

        if (action in possibleAttacks) player.strike(monsters[action.toInt() - 1])
            .also { if (it != 0) println("Монстр №$action получил $it повреждений") }
        else player.heal()

        monsters.sumOf { it.strike(player) }.also { if (it != 0) println("Игрок получил $it повреждений") }
    }
    println(if (player.isAlive) "Вы победили" else "Вы погибли")
}

abstract class Creature(private val attack: Int,
                        private val defence: Int,
                        val maxHealth: Int,
                        val damage: IntRange) {
    init {
        require(attack in 1..30) { "Атака $attack должна быть в интервале 1..30" }
        require(defence in 1..30) { "Защита $defence должна быть в интервале 1..30" }
        require(maxHealth >= 0) { "Максимальное здоровье $maxHealth должно быть больше либо рано 0" }
        damage.run {
            require(first >= 0) { "Минимальные повреждения $first должны быть больше или равны 0" }
            require(last >= 0) { "Максимальные повреждения $last должны быть больше или равны 0" }
            require(!isEmpty()) { "Максимальные повреждения $last должны быть не меньше минимальных $first" }
        }
    }

    var health: Int = maxHealth
        protected set(value) {
            field = if (!isAlive) 0 else max(0, value)
        }

    val isAlive: Boolean get() = health > 0

    fun strike(other: Creature): Int = if (isAlive) {
        val attackVal = max(attack - other.defence + 1, 1)
        val dices = List(attackVal) { (1..6).random() }
        if (dices.any { it >= 5 }) damage.random().also { other.health -= it }
        else 0
    } else 0
}

class Player(attack: Int, defence: Int, maxHealth: Int, damage: IntRange) :
    Creature(attack, defence, maxHealth, damage) {

    var remainingHeals = 4
        private set

    fun heal() {
        if (remainingHeals >= 0) {
            remainingHeals--
            health += (maxHealth / 10) * 3
        }
    }
}

class Monster(attack: Int, defence: Int, maxHealth: Int, damage: IntRange) :
    Creature(attack, defence, maxHealth, damage)

