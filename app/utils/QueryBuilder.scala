// In QueryBuilder.scala
package utils

import anorm.NamedParameter

import scala.util.Try

/** Represents a query part with its parameters
  * @param sql
  *   The SQL string with placeholders
  * @param params
  *   List of named parameters
  */
case class QueryPart(sql: String, orderSql: String = "", params: Seq[NamedParameter] = Seq.empty)

/** Utility object for building SQL queries with common patterns like pagination and search conditions.
  */
object QueryBuilder {

  /** Represents a search field with its parameters
    *
    * @param name
    *   Database column name
    * @param paramName
    *   Parameter name in the search map
    * @param operator
    *   SQL operator (=, >, <, LIKE, etc.)
    * @param dataType
    *   Optional data type for casting
    */
  case class SearchField(
      name: String,
      paramName: String,
      operator: String = "=",
      dataType: Option[String] = None
  )

  /** Builds the pagination part of a SQL query
    * @param page
    *   The current page number (1-based)
    * @return
    *   A tuple containing (startRow, limitationClause, parameters)
    */
  private def buildPagination(page: Int): (Int, String) = {
    val startRow   = Helper.start(page)
    val limitation = if page > 0 then s" LIMIT ${Helper.limit} OFFSET $startRow" else ""

    (startRow, limitation)
  }

  def safeCast(paramName: String, value: String, dataType: String): NamedParameter = {
    def fallback = NamedParameter(paramName, value)

    dataType.toLowerCase match {
      case "int"     => Try(NamedParameter(paramName, value.toInt)).getOrElse(fallback)
      case "float"   => Try(NamedParameter(paramName, value.toFloat)).getOrElse(fallback)
      case "double"  => Try(NamedParameter(paramName, value.toDouble)).getOrElse(fallback)
      case "boolean" => Try(NamedParameter(paramName, value.toBoolean)).getOrElse(fallback)
      case _         => fallback
    }
  }

  /** Adds a search condition to the query
    * @param search
    *   The search parameters map
    * @param field
    *   The database field name
    * @param searchTerm
    *   The key in the search map to look for
    * @param operator
    *   The operator to use (=, LIKE, >, etc.)
    * @param dataType
    *   Optional for casting the value to the correct data type
    * @return
    *   A QueryPart containing the condition and parameters
    */
  def addSearchCondition(
      search: Map[String, Option[String]],
      field: String,
      searchTerm: String,
      operator: String = "LIKE",
      dataType: String = ""
  ): QueryPart = {
    val rawValueOpt: Option[String] = search.get(searchTerm).flatten.filter(_.nonEmpty)

    // Extract value and optional sort order (e.g., "john:asc")
    val (valueOpt, orderSqlOpt): (Option[String], Option[String]) = {
      search
        .get(searchTerm)
        .flatten
        .map(value => if value.contains(":") then value else s"$value:_")
        .map(_.split(":", 2))
        .collect { case Array(v, o) =>
          val order    = o.toUpperCase
          val orderSql = if order == "ASC" || order == "DESC" then Some(s"$field $order") else None
          (Some(v), orderSql)
        }
        .getOrElse((None, None))
    }

    val validatedValue: Either[String, String] = valueOpt match {
      case Some(v) => SearchValidator.validateValue(v, operator)
      case None =>
        val op = operator.toUpperCase
        if op == "IS NULL" || op == "IS NOT NULL" then Right("")
        else Left(s"No value provided for '$searchTerm'.")
    }

    // Build search SQL and parameters
    val searchSqlAndParamsOpt: Option[(String, Seq[NamedParameter])] = {
      validatedValue match {
        case Left(errorMsg) =>
          println(s"[Search Validation] $errorMsg") // Atau log framework
          None

        case Right(rawValue) => {
          val op = operator.toUpperCase

          op match {
            // Tidak butuh value
            case "IS NULL" =>
              Some(s" AND $field IS NULL", Seq.empty)

            case "IS NOT NULL" =>
              Some(s" AND $field IS NOT NULL", Seq.empty)

            case _ =>
              valueOpt.flatMap { rawValue =>
                operator.toUpperCase match {
                  case "LIKE" | "ILIKE" =>
                    val paramSuffix = operator.toLowerCase
                    Some(
                      s" AND $field $operator {${searchTerm}_$paramSuffix}",
                      Seq(NamedParameter(s"${searchTerm}_$paramSuffix", s"%$rawValue%"))
                    )

                  case "IN" =>
                    val values = rawValue.split(",").map(_.trim).filter(_.nonEmpty).toSeq
                    if values.nonEmpty then {
                      val paramNames = values.indices.map(i => s"${searchTerm}_in_$i")
                      val namedParams = values.zipWithIndex.map { case (v, i) =>
                        safeCast(s"${searchTerm}_in_$i", v, dataType)
                      }
                      Some(
                        s" AND $field IN (${paramNames.map("{" + _ + "}").mkString(", ")})",
                        namedParams
                      )
                    } else None

                  case "BETWEEN" =>
                    val parts = rawValue.split(":").map(_.trim)
                    if parts.length == 2 && parts.forall(_.nonEmpty) then {
                      val paramStart = s"${searchTerm}_start"
                      val paramEnd   = s"${searchTerm}_end"
                      Some(
                        s" AND $field BETWEEN {$paramStart} AND {$paramEnd}",
                        Seq(
                          safeCast(paramStart, parts(0), dataType),
                          safeCast(paramEnd, parts(1), dataType)
                        )
                      )
                    } else None

                  // Default operator (=, >, <, >=, <=)
                  case _ =>
                    val paramName = s"${searchTerm}_op"
                    Some(
                      s" AND $field $operator {$paramName}",
                      Seq(safeCast(paramName, rawValue, dataType))
                    )
                }
              }
          }
        }
      }
    }

    QueryPart(
      searchSqlAndParamsOpt.map(_._1).getOrElse(""),
      orderSqlOpt.getOrElse(""),
      searchSqlAndParamsOpt.map(_._2).getOrElse(Seq.empty)
    )
  }

  // In QueryBuilder.scala
  /** Builds a complete SQL query with pagination and search conditions
    * @param baseQuery
    *   The base SQL query (should end with WHERE 1=1)
    * @param page
    *   The page number for pagination
    * @param search
    *   The search parameters map
    * @param searchFields
    *   List of search field definitions
    * @return
    *   A tuple containing (selectQuery, countQuery, limitation, parameters)
    */
  def buildQuery(
      baseQuery: String,
      page: Int,
      search: Map[String, Option[String]],
      searchFields: List[SearchField],
      extraConditions: Seq[QueryPart] = Seq.empty
  ): (String, String, String, List[NamedParameter]) = {
    val (_, limitation) = buildPagination(page)

    val baseConditions = searchFields.map { field =>
      addSearchCondition(
        search,
        field.name,
        field.paramName,
        field.operator,
        field.dataType.getOrElse("")
      )
    }

    val allConditions = baseConditions ++ extraConditions

    val whereClause = allConditions.map(_.sql).mkString(" ")
    val orderClause = allConditions.flatMap(c => Option(c.orderSql).filter(_.nonEmpty)) match {
      case Nil       => ""
      case orderList => s" ORDER BY ${orderList.mkString(", ")}"
    }
    val allParams = allConditions.flatMap(_.params)

    // Extract just the FROM and WHERE parts for the count query
    val fromWherePart = baseQuery.toLowerCase.indexOf(" from ") match {
      case idx if idx >= 0 => baseQuery.substring(idx)
      case _               => baseQuery
    }

    val selectQuery =
      s"""$baseQuery$whereClause$orderClause$limitation""".stripMargin
    val countQuery = s"SELECT COUNT(*) as total $fromWherePart$whereClause"

    (selectQuery, countQuery, limitation, allParams)
  }

}

/** */
object SearchValidator {

  def validateValue(value: String, operator: String): Either[String, String] = {
    val op = operator.toUpperCase

    op match {
      case "BETWEEN" =>
        val parts = value.split(":").map(_.trim)
        if parts.length == 2 && parts.forall(_.nonEmpty)
        then Right(value)
        else Left(s"Invalid format for BETWEEN. Expected format 'start:end', got '$value'.")

      case "IN" | "NOT IN" =>
        val values = value.split(",").map(_.trim).filter(_.nonEmpty)
        if values.nonEmpty
        then Right(value)
        else Left(s"Invalid IN list. Expected comma-separated values, got '$value'.")

      case "IS NULL" | "IS NOT NULL" =>
        Right(value) // tidak perlu validasi value

      case _ =>
        if value.nonEmpty
        then Right(value)
        else Left(s"Missing value for operator $operator.")
    }
  }
}

/** Adds a search condition to the query
  *
  * param search The search parameters map param field The database field name param searchTerm The key in the search
  * map to look for param operator The operator to use (=, LIKE, >, etc.) param dataType Optional for casting the value
  * to the correct data type return A QueryPart containing the condition and parameters
  */
//  def addSearchCondition(
//      search: Map[String, Option[String]],
//      field: String,
//      searchTerm: String,
//      operator: String = "LIKE",
//      prefix: String = ""
//  ): QueryPart = {
//    val paramName = s"${prefix}_$searchTerm".replace(".", "_")
//
//    search
//      .get(searchTerm)
//      .filter(_.nonEmpty)
//      .flatten
//      .map { value =>
//        operator.toUpperCase match {
//          case "LIKE" =>
//            QueryPart(
//              s" AND $field LIKE {${paramName}_like}",
//              Seq(NamedParameter(s"${paramName}_like", s"%$value%"))
//            )
//          case "ILIKE" =>
//            QueryPart(
//              s" AND $field ILIKE {${paramName}_ilike}",
//              Seq(NamedParameter(s"${paramName}_ilike", s"%$value%"))
//            )
//          case _ =>
//            QueryPart(
//              s" AND $field $operator {${paramName}_op}",
//              Seq(NamedParameter(s"${paramName}_op", value))
//            )
//        }
//      }
//      .getOrElse(QueryPart(""))
//  }
