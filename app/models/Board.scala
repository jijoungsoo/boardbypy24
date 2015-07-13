package models

import java.util.Date




case class Board(
    id:Long
    ,content: String
    ,writerDate: Date
    ,writerId:Long
    ,writerIp: String
    
)

