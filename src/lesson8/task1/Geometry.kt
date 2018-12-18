@file:Suppress("UNUSED_PARAMETER", "UNUSED_EXPRESSION")

package lesson8.task1

import lesson1.task1.sqr
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.*

/**
 * Точка на плоскости
 */
data class Point(val x: Double, val y: Double) {
    /**
     * Пример
     *
     * Рассчитать (по известной формуле) расстояние между двумя точками
     */
    fun distance(other: Point): Double = sqrt(sqr(x - other.x) + sqr(y - other.y))
}

/**
 * Треугольник, заданный тремя точками (a, b, c, см. constructor ниже).
 * Эти три точки хранятся в множестве points, их порядок не имеет значения.
 */
class Triangle private constructor(private val points: Set<Point>) {

    private val pointList = points.toList()

    val a: Point get() = pointList[0]

    val b: Point get() = pointList[1]

    val c: Point get() = pointList[2]

    constructor(a: Point, b: Point, c: Point) : this(linkedSetOf(a, b, c))

    /**
     * Пример: полупериметр
     */
    fun halfPerimeter() = (a.distance(b) + b.distance(c) + c.distance(a)) / 2.0

    /**
     * Пример: площадь
     */
    fun area(): Double {
        val p = halfPerimeter()
        return sqrt(p * (p - a.distance(b)) * (p - b.distance(c)) * (p - c.distance(a)))
    }

    /**
     * Пример: треугольник содержит точку
     */
    fun contains(p: Point): Boolean {
        val abp = Triangle(a, b, p)
        val bcp = Triangle(b, c, p)
        val cap = Triangle(c, a, p)
        return abp.area() + bcp.area() + cap.area() <= area()
    }

    override fun equals(other: Any?) = other is Triangle && points == other.points

    override fun hashCode() = points.hashCode()

    override fun toString() = "Triangle(a = $a, b = $b, c = $c)"
}

/**
 * Окружность с заданным центром и радиусом
 */
data class Circle(val center: Point, val radius: Double) {
    /**
     * Простая
     *
     * Рассчитать расстояние между двумя окружностями.
     * Расстояние между непересекающимися окружностями рассчитывается как
     * расстояние между их центрами минус сумма их радиусов.
     * Расстояние между пересекающимися окружностями считать равным 0.0.
     */
    fun distance(other: Circle): Double =
            if (other.radius + radius > center.distance(other.center))
                0.0
            else center.distance(other.center) - other.radius - radius

    /**
     * Тривиальная
     *
     * Вернуть true, если и только если окружность содержит данную точку НА себе или ВНУТРИ себя
     */
    fun contains(p: Point): Boolean = center.distance(p) <= radius
}

/**
 * Отрезок между двумя точками
 */
data class Segment(val begin: Point, val end: Point) {
    override fun equals(other: Any?) =
            other is Segment && (begin == other.begin && end == other.end || end == other.begin && begin == other.end)

    override fun hashCode() =
            begin.hashCode() + end.hashCode()
}

/**
 * Средняя
 *
 * Дано множество точек. Вернуть отрезок, соединяющий две наиболее удалённые из них.
 * Если в множестве менее двух точек, бросить IllegalArgumentException
 */
fun diameter(vararg points: Point): Segment {
    var p0 = Point(0.0, 0.0)
    var p1 = Point(0.0, 0.0)
    var max = 0.0
    if (points.size < 2)
        throw IllegalArgumentException()
    for (i in 0 until points.size - 1) {
        for (j in 1 until points.size) {
            val dist = points[i].distance(points[j])
            if (max < dist) {
                max = dist
                p0 = points[i]
                p1 = points[j]
            }
        }
    }
    return Segment(p0, p1)
}

/**
 * Простая
 *
 * Построить окружность по её диаметру, заданному двумя точками
 * Центр её должен находиться посередине между точками, а радиус составлять половину расстояния между ними
 */
fun circleByDiameter(diameter: Segment): Circle {
    val center = Point(((diameter.begin.x + diameter.end.x) / 2), ((diameter.begin.y + diameter.end.y) / 2))
    val radius = center.distance(diameter.begin)
    return Circle(center, radius)
}

/**
 * Прямая, заданная точкой point и углом наклона angle (в радианах) по отношению к оси X.
 * Уравнение прямой: (y - point.y) * cos(angle) = (x - point.x) * sin(angle)
 * или: y * cos(angle) = x * sin(angle) + b, где b = point.y * cos(angle) - point.x * sin(angle).
 * Угол наклона обязан находиться в диапазоне от 0 (включительно) до PI (исключительно).
 */
class Line private constructor(val b: Double, val angle: Double) {
    init {
        require(angle >= 0 && angle < PI) { "Incorrect line angle: $angle" }
    }

    constructor(point: Point, angle: Double) : this(point.y * cos(angle) - point.x * sin(angle), angle)

    /**
     * Средняя
     *
     * Найти точку пересечения с другой линией.
     * Для этого необходимо составить и решить систему из двух уравнений (каждое для своей прямой)
     */
    fun crossPoint(other: Line): Point {
        val x = (b * cos(other.angle) - other.b * cos(angle)) /
                (sin(other.angle - angle))
        return if (abs(cos(angle)) <= abs(cos(other.angle)))
            Point(x, (x * sin(other.angle) + other.b) / cos(other.angle))
        else Point(x, (x * sin(angle) + b) / cos(angle))
    }

    override fun equals(other: Any?) = other is Line && angle == other.angle && b == other.b

    override fun hashCode(): Int {
        var result = b.hashCode()
        result = 31 * result + angle.hashCode()
        return result
    }

    override fun toString() = "Line(${cos(angle)} * y = ${sin(angle)} * x + $b)"
}

/**
 * Средняя
 *
 * Построить прямую по отрезку
 */
fun lineBySegment(s: Segment): Line =  lineByPoints(s.begin, s.end)


/**
 * Средняя
 *
 * Построить прямую по двум точкам
 */
fun lineByPoints(a: Point, b: Point): Line {
    val zn = (atan((a.y - b.y) / (a.x - b.x)) + 2 * PI) % PI
    return Line(a, zn)
}

/**
 * Сложная
 *
 * Построить серединный перпендикуляр по отрезку или по двум точкам
 */
fun bisectorByPoints(a: Point, b: Point): Line {
    val center = Point(((a.x + b.x) / 2), ((a.y + b.y) / 2))
    val angle = (atan((a.y - b.y) / (a.x - b.x)) + PI / 2) % PI
    return Line(center, angle)
}

/**
 * Средняя
 *
 * Задан список из n окружностей на плоскости. Найти пару наименее удалённых из них.
 * Если в списке менее двух окружностей, бросить IllegalArgumentException
 */
fun findNearestCirclePair(vararg circles: Circle): Pair<Circle, Circle> {
    if (circles.size < 2)
        throw IllegalArgumentException()
    var c0 = circles[0]
    var c1 = circles[1]
    var min = Double.MAX_VALUE
    for (i in 0 until circles.size - 1)
        for (j in i + 1 until circles.size) {
            val per = circles[i].distance(circles[j])
            if (per < min) {
                min = per
                c0 = circles[i]
                c1 = circles[j]
            }
        }
    return c0 to c1
}

/**
 * Сложная
 *
 * Дано три различные точки. Построить окружность, проходящую через них
 * (все три точки должны лежать НА, а не ВНУТРИ, окружности).
 * Описание алгоритмов см. в Интернете
 * (построить окружность по трём точкам, или
 * построить окружность, описанную вокруг треугольника - эквивалентная задача).
 */
fun circleByThreePoints(a: Point, b: Point, c: Point): Circle {
    val ab = bisectorByPoints(a, b)
    val bc = bisectorByPoints(b, c)
    val point = ab.crossPoint(bc)
    return Circle(point, point.distance(a))
}

/**
 * Очень сложная
 *
 * Дано множество точек на плоскости. Найти круг минимального радиуса,
 * содержащий все эти точки. Если множество пустое, бросить IllegalArgumentException.
 * Если множество содержит одну точку, вернуть круг нулевого радиуса с центром в данной точке.
 *
 * Примечание: в зависимости от ситуации, такая окружность может либо проходить через какие-либо
 * три точки данного множества, либо иметь своим диаметром отрезок,
 * соединяющий две самые удалённые точки в данном множестве.
 */
fun minContainingCircle(vararg points: Point): Circle {
    var minCircle = Circle(Point(0.0, 0.0), Double.MAX_VALUE)
    when {
        points.isEmpty() ->
            throw IllegalArgumentException()
        points.size == 1 ->
            return Circle(points[0], 0.0)
        points.size == 2 ->
            return circleByDiameter(Segment(points[0], points[1]))
        points.size == 3 ->
            return circleByThreePoints(points[0], points[1], points[2])
    }
    for (i in 0 until points.size - 1)
        for (j in i + 1 until points.size) {
            val current = circleByDiameter(Segment(points[i], points[j]))
            val contain = points.all { current.contains(it) }
            if (contain && current.radius < minCircle.radius)
                minCircle = current
        }
    for (i in 0 until points.size - 2)
        for (j in i + 1 until points.size - 1)
            for (k in j + 1 until points.size) {
                val current = circleByThreePoints(points[i], points[j], points[k])
                val contain = points.all { current.contains(it) }
                if (contain && current.radius < minCircle.radius)
                    minCircle = current
            }
    return minCircle
}

