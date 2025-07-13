package utils

object Helper{
  val limit: Int = 3
  
  def start(page: Int, limiter: Int = 0): Int = {
    (page - 1) * (if (limiter != 0) limiter else limit)
  }
}
