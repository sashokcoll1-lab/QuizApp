package ru.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class QuizActivity : AppCompatActivity() {
    private val questionList = mutableListOf<Question>()

    private lateinit var tvQuestionCount: TextView
    private lateinit var tvQuestionText: TextView
    private lateinit var btnOptions: List<Button>

    private var currentQuestionIndex = 0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        tvQuestionText = findViewById(R.id.tvQuestionText)
        btnOptions = listOf(
            findViewById(R.id.btnOption1),
            findViewById(R.id.btnOption2),
            findViewById(R.id.btnOption3),
            findViewById(R.id.btnOption4)
        )

        loadQuestionsFromAssets()

        btnOptions.forEachIndexed { index, button ->
            button.setOnClickListener { checkAnswer(index) }
        }

        showNextQuestion()
    }

    private fun loadQuestionsFromAssets() {
        questionList.clear()
        try {
            BufferedReader(InputStreamReader(assets.open("questions.txt"), Charsets.UTF_8)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isNotBlank()) {
                        val parts = line.split("|").map { it.trim() }
                        if (parts.size == 6) {
                            val correctIndex = parts[5].toIntOrNull()
                            if (correctIndex != null && correctIndex in 0..3) {
                                questionList.add(
                                    Question(
                                        questionText = parts[0],
                                        options = parts.subList(1, 5),
                                        correctIndex = correctIndex
                                    )
                                )
                            }
                        }
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            tvQuestionText.text = "Не удалось загрузить вопросы: ${e.localizedMessage}"
            btnOptions.forEach { it.isEnabled = false }
        }
    }

    private fun showNextQuestion() {
        if (questionList.isEmpty()) {
            tvQuestionCount.text = "Нет вопросов"
            tvQuestionText.text = "Проверь файл assets/questions.txt"
            btnOptions.forEach { it.isEnabled = false }
            return
        }

        if (currentQuestionIndex >= questionList.size) {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra(ResultActivity.EXTRA_SCORE, score)
            intent.putExtra(ResultActivity.EXTRA_TOTAL, questionList.size)
            startActivity(intent)
            finish()
            return
        }

        val question = questionList[currentQuestionIndex]
        tvQuestionCount.text = "Вопрос ${currentQuestionIndex + 1} из ${questionList.size}"
        tvQuestionText.text = question.questionText

        btnOptions.forEachIndexed { index, button ->
            button.text = question.options[index]
            button.isEnabled = true
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        if (currentQuestionIndex >= questionList.size) return

        if (selectedIndex == questionList[currentQuestionIndex].correctIndex) {
            score++
        }

        currentQuestionIndex++
        showNextQuestion()
    }
}
