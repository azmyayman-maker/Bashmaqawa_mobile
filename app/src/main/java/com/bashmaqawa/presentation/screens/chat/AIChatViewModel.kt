package com.bashmaqawa.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Chat Message Model
 * نموذج رسالة المحادثة
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: MessageStatus = MessageStatus.SENT
) {
    val formattedTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

enum class MessageStatus {
    SENDING, SENT, ERROR
}

/**
 * AI Chat UI State
 * حالة واجهة المحادثة الذكية
 */
data class AIChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            content = "مرحباً! أنا مساعد بشمقاول الذكي. كيف يمكنني مساعدتك اليوم؟",
            isUser = false
        )
    ),
    val isTyping: Boolean = false,
    val inputText: String = "",
    val isConnected: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Suggestion Chips for Quick Actions
 */
data class SuggestionChip(
    val text: String,
    val icon: String? = null
)

val defaultSuggestions = listOf(
    SuggestionChip("كم عدد العمال النشطين؟", "👷"),
    SuggestionChip("ما هي مصروفات اليوم؟", "💰"),
    SuggestionChip("أظهر المشاريع الجارية", "🏗️"),
    SuggestionChip("ملخص هذا الشهر", "📊")
)

/**
 * AI Chat ViewModel with State Management
 * ViewModel للمحادثة الذكية مع إدارة الحالة
 */
@HiltViewModel
class AIChatViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()
    
    val suggestions = defaultSuggestions
    
    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage(content: String = _uiState.value.inputText) {
        if (content.isBlank()) return
        
        val userMessage = ChatMessage(
            content = content.trim(),
            isUser = true,
            status = MessageStatus.SENT
        )
        
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputText = "",
                isTyping = true
            )
        }
        
        // Simulate AI response
        viewModelScope.launch {
            delay(1500) // Simulate thinking
            
            val response = generateResponse(content)
            val aiMessage = ChatMessage(
                content = response,
                isUser = false
            )
            
            _uiState.update { state ->
                state.copy(
                    messages = state.messages + aiMessage,
                    isTyping = false
                )
            }
        }
    }
    
    fun sendSuggestion(suggestion: SuggestionChip) {
        sendMessage(suggestion.text)
    }
    
    fun retryMessage(messageId: String) {
        val message = _uiState.value.messages.find { it.id == messageId }
        if (message != null && message.status == MessageStatus.ERROR) {
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.filter { it.id != messageId }
                )
            }
            sendMessage(message.content)
        }
    }
    
    fun copyMessage(content: String) {
        // Clipboard handling would be done in the UI layer
        _uiState.update { it.copy(errorMessage = "تم النسخ!") }
        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(errorMessage = null) }
        }
    }
    
    fun clearChat() {
        _uiState.update { 
            AIChatUiState(
                messages = listOf(
                    ChatMessage(
                        content = "مرحباً! أنا مساعد بشمقاول الذكي. كيف يمكنني مساعدتك اليوم؟",
                        isUser = false
                    )
                )
            )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Generate contextual AI response
     * This is a placeholder - in production, this would call an AI service
     */
    private fun generateResponse(query: String): String {
        val lowerQuery = query.lowercase()
        
        return when {
            lowerQuery.contains("عمال") || lowerQuery.contains("عامل") ->
                "📊 لديك حالياً عمال نشطين في النظام. يمكنك إدارتهم من صفحة القوى العاملة."
            
            lowerQuery.contains("مصروف") || lowerQuery.contains("مصاريف") ->
                "💰 المصروفات مسجلة في النظام.\n\nيمكنك مشاهدة التفاصيل من صفحة المالية أو التحليلات."
            
            lowerQuery.contains("مشروع") || lowerQuery.contains("مشاريع") ->
                "🏗️ المشاريع:\n• يمكنك إضافة مشروع جديد\n• تعيين عمال للمشاريع\n• تتبع المصروفات لكل مشروع\n\nافتح صفحة المشاريع للمزيد."
            
            lowerQuery.contains("ملخص") || lowerQuery.contains("تقرير") ->
                "📈 ملخص النظام:\n\n• إجمالي العمال: متوفر في التحليلات\n• المشاريع النشطة: راجع صفحة المشاريع\n• الوضع المالي: متوفر في صفحة المالية\n\nهل تريد معرفة المزيد عن شيء محدد؟"
            
            lowerQuery.contains("مساعد") || lowerQuery.contains("مرحبا") || lowerQuery.contains("اهلا") ->
                "أهلاً بك! 👋\n\nأنا هنا لمساعدتك في:\n• إدارة العمال والحضور\n• متابعة المشاريع\n• تتبع المصروفات والإيرادات\n• الحصول على تقارير وإحصائيات\n\nما الذي تحتاجه؟"
            
            lowerQuery.contains("حضور") || lowerQuery.contains("غياب") ->
                "📅 نظام الحضور:\n\n• سجل الحضور اليومي من التقويم\n• تقارير الحضور متاحة شهرياً\n• التكامل مع رواتب العمال\n\nهل تريد تسجيل حضور اليوم؟"
            
            lowerQuery.contains("راتب") || lowerQuery.contains("رواتب") || lowerQuery.contains("مستحقات") ->
                "💵 الرواتب والمستحقات:\n\n• تحسب تلقائياً من الحضور\n• السلف والخصومات مسجلة\n• الصافي = المستحقات - السلف - الخصومات\n\nراجع تفاصيل العامل لمعرفة رصيده."
            
            else ->
                "شكراً لسؤالك! 🤔\n\nهذه الميزة تجريبية وقيد التطوير.\n\nيمكنني حالياً مساعدتك في:\n• معلومات عن العمال والمشاريع\n• استفسارات مالية عامة\n• تقارير وإحصائيات\n\nجرب أحد الاقتراحات أدناه!"
        }
    }
}
