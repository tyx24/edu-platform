<template>
  <div class="exam-detail-container">
    <!-- 教师管理考试视图 -->
    <div v-if="isTeacher && !isStudentView" class="teacher-view">
      <div class="page-header">
        <h1>{{ exam.title }}</h1>
        <p>{{ exam.description }}</p>
        <div class="exam-meta">
          <span>开始时间: {{ formatDateTime(exam.startTime) }}</span>
          <span>结束时间: {{ formatDateTime(exam.endTime) }}</span>
          <span>考试时长: {{ exam.duration }} 分钟</span>
          <span>总分: {{ exam.totalScore }} 分</span>
          <span>及格分数: {{ exam.passingScore }} 分</span>
        </div>
      </div>
      
      <div class="action-bar">
        <el-button type="primary" @click="showAddQuestionDialog">
          <el-icon><Plus /></el-icon> 添加题目
        </el-button>
        <el-button type="success" @click="viewExamRecords">
          <el-icon><Document /></el-icon> 查看考试记录
        </el-button>
        <el-button @click="$router.push(`/course/${exam.courseId}/exams`)">
          返回考试列表
        </el-button>
      </div>
      
      <div class="questions-section">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="questions.length === 0" class="empty-state">
          <el-empty description="暂无题目" />
          <el-button type="primary" @click="showAddQuestionDialog">
            添加题目
          </el-button>
        </div>
        
        <div v-else class="questions-list">
          <el-card 
            v-for="(question, index) in questions" 
            :key="question.id"
            class="question-card"
            shadow="hover"
          >
            <div class="question-header">
              <div class="question-title">
                <span class="question-number">{{ index + 1 }}</span>
                <span class="question-type-tag">
                  {{ getQuestionTypeText(question.type) }}
                </span>
                <span class="question-score">{{ question.score }} 分</span>
              </div>
              <div class="question-actions">
                <el-button type="primary" size="small" @click="editQuestion(question)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="deleteQuestion(question)">
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="question-content">
              <div class="question-text">{{ question.content }}</div>
              
              <!-- 选择题选项 -->
              <div v-if="question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'" class="question-options">
                <div 
                  v-for="option in question.options" 
                  :key="option.id"
                  class="question-option"
                  :class="{ 'correct-option': option.isCorrect }"
                >
                  <span class="option-label">{{ option.label }}.</span>
                  <span class="option-content">{{ option.content }}</span>
                </div>
              </div>
              
              <!-- 判断题答案 -->
              <div v-else-if="question.type === 'TRUE_FALSE'" class="question-true-false">
                <div class="correct-answer">
                  正确答案: {{ question.answer === 'true' ? '正确' : '错误' }}
                </div>
              </div>
              
              <!-- 填空题答案 -->
              <div v-else-if="question.type === 'FILL_BLANK'" class="question-fill-blank">
                <div class="correct-answer">
                  正确答案: {{ question.answer }}
                </div>
              </div>
              
              <!-- 简答题参考答案 -->
              <div v-else-if="question.type === 'SHORT_ANSWER'" class="question-short-answer">
                <div class="reference-answer">
                  参考答案: {{ question.referenceAnswer }}
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 考试记录对话框 -->
      <el-dialog
        v-model="recordsDialogVisible"
        title="考试记录"
        width="80%"
      >
        <div v-if="examRecords.length === 0" class="empty-state">
          <el-empty description="暂无考试记录" />
        </div>
        
        <el-table v-else :data="examRecords" style="width: 100%">
          <el-table-column prop="studentName" label="学生姓名" />
          <el-table-column prop="submitTime" label="提交时间">
            <template #default="scope">
              {{ formatDateTime(scope.row.submitTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分">
            <template #default="scope">
              {{ scope.row.score }} / {{ exam.totalScore }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="用时">
            <template #default="scope">
              {{ formatDuration(scope.row.duration) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.score >= exam.passingScore ? 'success' : 'danger'">
                {{ scope.row.score >= exam.passingScore ? '通过' : '未通过' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewStudentAnswers(scope.row)">
                查看答卷
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
      
      <!-- 添加/编辑题目对话框 -->
      <el-dialog
        v-model="questionDialogVisible"
        :title="isEditQuestionMode ? '编辑题目' : '添加题目'"
        width="60%"
      >
        <el-form :model="questionForm" :rules="questionRules" ref="questionFormRef" label-width="100px">
          <el-form-item label="题目类型" prop="type">
            <el-select v-model="questionForm.type" placeholder="请选择题目类型">
              <el-option label="单选题" value="SINGLE_CHOICE" />
              <el-option label="多选题" value="MULTIPLE_CHOICE" />
              <el-option label="判断题" value="TRUE_FALSE" />
              <el-option label="填空题" value="FILL_BLANK" />
              <el-option label="简答题" value="SHORT_ANSWER" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="题目内容" prop="content">
            <el-input 
              v-model="questionForm.content" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入题目内容"
            />
          </el-form-item>
          
          <el-form-item label="分值" prop="score">
            <el-input-number v-model="questionForm.score" :min="1" :max="100" />
          </el-form-item>
          
          <!-- 选择题选项 -->
          <template v-if="questionForm.type === 'SINGLE_CHOICE' || questionForm.type === 'MULTIPLE_CHOICE'">
            <el-divider>选项</el-divider>
            
            <div 
              v-for="(option, index) in questionForm.options" 
              :key="index"
              class="option-form-item"
            >
              <el-form-item :label="`选项 ${option.label}`" :prop="`options.${index}.content`" :rules="[{ required: true, message: '请输入选项内容', trigger: 'blur' }]">
                <div class="option-input-group">
                  <el-input v-model="option.content" placeholder="请输入选项内容" />
                  <el-checkbox 
                    v-model="option.isCorrect"
                    :disabled="questionForm.type === 'SINGLE_CHOICE' && hasSelectedOption && !option.isCorrect"
                  >
                    正确答案
                  </el-checkbox>
                  <el-button 
                    v-if="questionForm.options.length > 2"
                    type="danger" 
                    circle 
                    @click="removeOption(index)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </el-form-item>
            </div>
            
            <el-form-item>
              <el-button type="primary" @click="addOption">
                添加选项
              </el-button>
            </el-form-item>
          </template>
          
          <!-- 判断题答案 -->
          <template v-else-if="questionForm.type === 'TRUE_FALSE'">
            <el-form-item label="正确答案" prop="answer">
              <el-radio-group v-model="questionForm.answer">
                <el-radio label="true">正确</el-radio>
                <el-radio label="false">错误</el-radio>
              </el-radio-group>
            </el-form-item>
          </template>
          
          <!-- 填空题答案 -->
          <template v-else-if="questionForm.type === 'FILL_BLANK'">
            <el-form-item label="正确答案" prop="answer">
              <el-input v-model="questionForm.answer" placeholder="请输入正确答案" />
            </el-form-item>
          </template>
          
          <!-- 简答题参考答案 -->
          <template v-else-if="questionForm.type === 'SHORT_ANSWER'">
            <el-form-item label="参考答案" prop="referenceAnswer">
              <el-input 
                v-model="questionForm.referenceAnswer" 
                type="textarea" 
                :rows="3" 
                placeholder="请输入参考答案"
              />
            </el-form-item>
          </template>
        </el-form>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="questionDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitQuestionForm">确定</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
    
    <!-- 学生参加考试视图 -->
    <div v-else-if="isStudent && !examSubmitted" class="student-view">
      <div class="exam-header">
        <div class="exam-info">
          <h1>{{ exam.title }}</h1>
          <div class="exam-meta">
            <span>总分: {{ exam.totalScore }} 分</span>
            <span>考试时长: {{ exam.duration }} 分钟</span>
            <span>及格分数: {{ exam.passingScore }} 分</span>
          </div>
        </div>
        
        <div class="exam-timer">
          <div class="timer-label">剩余时间</div>
          <div class="timer-value">{{ formatRemainingTime }}</div>
        </div>
      </div>
      
      <div class="exam-content">
        <div class="questions-navigation">
          <div class="navigation-header">题目导航</div>
          <div class="navigation-buttons">
            <el-button 
              v-for="(question, index) in questions" 
              :key="question.id"
              size="small"
              :type="getQuestionButtonType(index)"
              @click="currentQuestionIndex = index"
            >
              {{ index + 1 }}
            </el-button>
          </div>
          
          <div class="submit-section">
            <el-button type="danger" @click="confirmSubmitExam">
              交卷
            </el-button>
          </div>
        </div>
        
        <div class="question-container">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="questions.length === 0" class="empty-state">
            <el-empty description="考试暂无题目" />
            <el-button @click="$router.push(`/course/${exam.courseId}/exams`)">
              返回考试列表
            </el-button>
          </div>
          
          <div v-else class="current-question">
            <div class="question-header">
              <div class="question-title">
                <span class="question-number">{{ currentQuestionIndex + 1 }}</span>
                <span class="question-type-tag">
                  {{ getQuestionTypeText(currentQuestion.type) }}
                </span>
                <span class="question-score">{{ currentQuestion.score }} 分</span>
              </div>
            </div>
            
            <div class="question-content">
              <div class="question-text">{{ currentQuestion.content }}</div>
              
              <!-- 单选题 -->
              <div v-if="currentQuestion.type === 'SINGLE_CHOICE'" class="question-options">
                <el-radio-group v-model="studentAnswers[currentQuestionIndex].answer">
                  <div 
                    v-for="option in currentQuestion.options" 
                    :key="option.id"
                    class="question-option"
                  >
                    <el-radio :label="option.id">
                      <span class="option-label">{{ option.label }}.</span>
                      <span class="option-content">{{ option.content }}</span>
                    </el-radio>
                  </div>
                </el-radio-group>
              </div>
              
              <!-- 多选题 -->
              <div v-else-if="currentQuestion.type === 'MULTIPLE_CHOICE'" class="question-options">
                <el-checkbox-group v-model="studentAnswers[currentQuestionIndex].answer">
                  <div 
                    v-for="option in currentQuestion.options" 
                    :key="option.id"
                    class="question-option"
                  >
                    <el-checkbox :label="option.id">
                      <span class="option-label">{{ option.label }}.</span>
                      <span class="option-content">{{ option.content }}</span>
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>
              
              <!-- 判断题 -->
              <div v-else-if="currentQuestion.type === 'TRUE_FALSE'" class="question-true-false">
                <el-radio-group v-model="studentAnswers[currentQuestionIndex].answer">
                  <el-radio label="true">正确</el-radio>
                  <el-radio label="false">错误</el-radio>
                </el-radio-group>
              </div>
              
              <!-- 填空题 -->
              <div v-else-if="currentQuestion.type === 'FILL_BLANK'" class="question-fill-blank">
                <el-input 
                  v-model="studentAnswers[currentQuestionIndex].answer" 
                  placeholder="请输入答案"
                />
              </div>
              
              <!-- 简答题 -->
              <div v-else-if="currentQuestion.type === 'SHORT_ANSWER'" class="question-short-answer">
                <el-input 
                  v-model="studentAnswers[currentQuestionIndex].answer" 
                  type="textarea" 
                  :rows="5" 
                  placeholder="请输入答案"
                />
              </div>
            </div>
            
            <div class="question-navigation">
              <el-button 
                :disabled="currentQuestionIndex === 0" 
                @click="previousQuestion"
              >
                上一题
              </el-button>
              <el-button 
                :disabled="currentQuestionIndex === questions.length - 1" 
                type="primary" 
                @click="nextQuestion"
              >
                下一题
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 考试结果视图 -->
    <div v-else-if="isStudent && examSubmitted" class="result-view">
      <div class="result-header">
        <h1>{{ exam.title }} - 考试结果</h1>
      </div>
      
      <el-card class="result-card">
        <div class="result-summary">
          <div class="result-score">
            <div class="score-value">{{ examRecord.score }}</div>
            <div class="score-total">/ {{ exam.totalScore }}</div>
          </div>
          
          <div class="result-status">
            <el-tag :type="examRecord.score >= exam.passingScore ? 'success' : 'danger'" size="large">
              {{ examRecord.score >= exam.passingScore ? '通过' : '未通过' }}
            </el-tag>
          </div>
          
          <div class="result-meta">
            <div class="meta-item">
              <span class="meta-label">提交时间:</span>
              <span class="meta-value">{{ formatDateTime(examRecord.submitTime) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">用时:</span>
              <span class="meta-value">{{ formatDuration(examRecord.duration) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">及格分数:</span>
              <span class="meta-value">{{ exam.passingScore }} 分</span>
            </div>
          </div>
        </div>
        
        <el-divider>答题详情</el-divider>
        
        <div class="answer-details">
          <el-collapse>
            <el-collapse-item 
              v-for="(answer, index) in examRecord.answers" 
              :key="answer.questionId"
              :title="`第 ${index + 1} 题 (${getQuestionTypeText(answer.questionType)}) - ${answer.score} 分`"
              :name="index"
            >
              <div class="answer-detail">
                <div class="question-content">{{ answer.questionContent }}</div>
                
                <div class="answer-content">
                  <div class="answer-label">你的答案:</div>
                  <div class="answer-value">
                    <!-- 单选题 -->
                    <template v-if="answer.questionType === 'SINGLE_CHOICE'">
                      {{ getOptionContent(answer.questionId, answer.answer) }}
                    </template>
                    
                    <!-- 多选题 -->
                    <template v-else-if="answer.questionType === 'MULTIPLE_CHOICE'">
                      <div v-for="optionId in answer.answer" :key="optionId">
                        {{ getOptionContent(answer.questionId, optionId) }}
                      </div>
                    </template>
                    
                    <!-- 判断题 -->
                    <template v-else-if="answer.questionType === 'TRUE_FALSE'">
                      {{ answer.answer === 'true' ? '正确' : '错误' }}
                    </template>
                    
                    <!-- 填空题和简答题 -->
                    <template v-else>
                      {{ answer.answer }}
                    </template>
                  </div>
                </div>
                
                <div class="correct-answer">
                  <div class="answer-label">正确答案:</div>
                  <div class="answer-value">
                    <!-- 单选题 -->
                    <template v-if="answer.questionType === 'SINGLE_CHOICE'">
                      {{ getCorrectOptionContent(answer.questionId) }}
                    </template>
                    
                    <!-- 多选题 -->
                    <template v-else-if="answer.questionType === 'MULTIPLE_CHOICE'">
                      <div v-for="option in getCorrectOptions(answer.questionId)" :key="option.id">
                        {{ option.label }}. {{ option.content }}
                      </div>
                    </template>
                    
                    <!-- 判断题 -->
                    <template v-else-if="answer.questionType === 'TRUE_FALSE'">
                      {{ getQuestionById(answer.questionId).answer === 'true' ? '正确' : '错误' }}
                    </template>
                    
                    <!-- 填空题 -->
                    <template v-else-if="answer.questionType === 'FILL_BLANK'">
                      {{ getQuestionById(answer.questionId).answer }}
                    </template>
                    
                    <!-- 简答题 -->
                    <template v-else-if="answer.questionType === 'SHORT_ANSWER'">
                      {{ getQuestionById(answer.questionId).referenceAnswer }}
                    </template>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
        
        <div class="result-actions">
          <el-button @click="$router.push(`/course/${exam.courseId}/exams`)">
            返回考试列表
          </el-button>
          <el-button type="primary" @click="$router.push(`/course/${exam.courseId}`)">
            返回课程
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete, Document } from '@element-plus/icons-vue';
import { examApi } from '../../api/exam';
import { questionApi } from '../../api/question';
import { examRecordApi } from '../../api/examRecord';
import { useAuthStore } from '../../store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 考试ID
const examId = computed(() => route.params.examId);

// 用户角色
const isTeacher = computed(() => authStore.hasPermission('teacher'));
const isStudent = computed(() => authStore.hasPermission('student'));

// 是否为学生视图（参加考试）
const isStudentView = computed(() => route.path.includes('/take'));

// 考试数据
const loading = ref(true);
const exam = ref({});
const questions = ref([]);

// 教师管理考试相关
const questionDialogVisible = ref(false);
const isEditQuestionMode = ref(false);
const currentEditQuestion = ref(null);
const questionFormRef = ref(null);

// 考试记录相关
const recordsDialogVisible = ref(false);
const examRecords = ref([]);

// 学生参加考试相关
const currentQuestionIndex = ref(0);
const studentAnswers = ref([]);
const examStartTime = ref(null);
const remainingTime = ref(0);
const timerInterval = ref(null);
const examSubmitted = ref(false);
const examRecord = ref(null);

// 当前题目
const currentQuestion = computed(() => {
  if (questions.value.length === 0 || currentQuestionIndex.value >= questions.value.length) {
    return {};
  }
  return questions.value[currentQuestionIndex.value];
});

// 剩余时间格式化
const formatRemainingTime = computed(() => {
  const hours = Math.floor(remainingTime.value / 3600);
  const minutes = Math.floor((remainingTime.value % 3600) / 60);
  const seconds = remainingTime.value % 60;
  
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
});

// 题目表单
const questionForm = ref({
  type: 'SINGLE_CHOICE',
  content: '',
  score: 5,
  options: [
    { label: 'A', content: '', isCorrect: false },
    { label: 'B', content: '', isCorrect: false },
    { label: 'C', content: '', isCorrect: false },
    { label: 'D', content: '', isCorrect: false }
  ],
  answer: '',
  referenceAnswer: ''
});

// 是否有选中的选项（单选题）
const hasSelectedOption = computed(() => {
  if (questionForm.value.type !== 'SINGLE_CHOICE') return false;
  return questionForm.value.options.some(option => option.isCorrect);
});

// 表单验证规则
const questionRules = {
  type: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  score: [
    { required: true, message: '请设置分值', trigger: 'change' }
  ],
  answer: [
    { 
      required: true, 
      message: '请输入正确答案', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (questionForm.value.type === 'SINGLE_CHOICE' || questionForm.value.type === 'MULTIPLE_CHOICE') {
          // 选择题通过选项的isCorrect判断
          const hasCorrect = questionForm.value.options.some(option => option.isCorrect);
          if (!hasCorrect) {
            callback(new Error('请选择至少一个正确答案'));
          } else {
            callback();
          }
        } else if (questionForm.value.type === 'TRUE_FALSE') {
          if (!value) {
            callback(new Error('请选择正确或错误'));
          } else {
            callback();
          }
        } else if (questionForm.value.type === 'FILL_BLANK') {
          if (!value) {
            callback(new Error('请输入正确答案'));
          } else {
            callback();
          }
        } else {
          callback();
        }
      }
    }
  ],
  referenceAnswer: [
    { 
      required: true, 
      message: '请输入参考答案', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (questionForm.value.type === 'SHORT_ANSWER' && !value) {
          callback(new Error('请输入参考答案'));
        } else {
          callback();
        }
      }
    }
  ]
};

// 获取考试信息
const fetchExamInfo = async () => {
  loading.value = true;
  try {
    // 这里假设API返回的数据结构
    const res = await examApi.getExamById(examId.value);
    exam.value = res.data || {};
  } catch (error) {
    console.error('获取考试信息失败:', error);
    ElMessage.error('获取考试信息失败');
  } finally {
    loading.value = false;
  }
};

// 获取考试题目
const fetchQuestions = async () => {
  loading.value = true;
  try {
    let res;
    if (isTeacher.value && !isStudentView.value) {
      // 教师获取所有题目信息（包含答案）
      res = await questionApi.getQuestionList(examId.value);
    } else if (isStudent.value && isStudentView.value) {
      // 学生获取考试题目（不包含答案）
      res = await questionApi.getStudentExamQuestions(examId.value);
    }
    
    questions.value = res.data || [];
    
    // 如果是学生参加考试，初始化答案数组
    if (isStudent.value && isStudentView.value) {
      initStudentAnswers();
      startExamTimer();
    }
  } catch (error) {
    console.error('获取考试题目失败:', error);
    ElMessage.error('获取考试题目失败');
  } finally {
    loading.value = false;
  }
};

// 获取考试记录
const fetchExamRecords = async () => {
  try {
    const res = await examRecordApi.getExamRecords(examId.value);
    examRecords.value = res.data || [];
  } catch (error) {
    console.error('获取考试记录失败:', error);
    ElMessage.error('获取考试记录失败');
  }
};

// 获取学生考试记录
const fetchMyExamRecord = async () => {
  try {
    const res = await examRecordApi.getMyExamRecord(examId.value);
    if (res.data) {
      examRecord.value = res.data;
      examSubmitted.value = true;
    }
  } catch (error) {
    console.error('获取考试记录失败:', error);
  }
};

// 显示添加题目对话框
const showAddQuestionDialog = () => {
  isEditQuestionMode.value = false;
  currentEditQuestion.value = null;
  questionForm.value = {
    type: 'SINGLE_CHOICE',
    content: '',
    score: 5,
    options: [
      { label: 'A', content: '', isCorrect: false },
      { label: 'B', content: '', isCorrect: false },
      { label: 'C', content: '', isCorrect: false },
      { label: 'D', content: '', isCorrect: false }
    ],
    answer: '',
    referenceAnswer: ''
  };
  questionDialogVisible.value = true;
};

// 编辑题目
const editQuestion = (question) => {
  isEditQuestionMode.value = true;
  currentEditQuestion.value = question;
  
  // 根据题目类型设置表单
  questionForm.value = {
    type: question.type,
    content: question.content,
    score: question.score,
    options: question.options ? [...question.options] : [],
    answer: question.answer || '',
    referenceAnswer: question.referenceAnswer || ''
  };
  
  questionDialogVisible.value = true;
};

// 添加选项
const addOption = () => {
  const labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  const nextIndex = questionForm.value.options.length;
  
  if (nextIndex < labels.length) {
    questionForm.value.options.push({
      label: labels[nextIndex],
      content: '',
      isCorrect: false
    });
  }
};

// 移除选项
const removeOption = (index) => {
  questionForm.value.options.splice(index, 1);
  
  // 重新分配标签
  const labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  questionForm.value.options.forEach((option, idx) => {
    option.label = labels[idx];
  });
};

// 提交题目表单
const submitQuestionForm = async () => {
  if (!questionFormRef.value) return;
  
  await questionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理选择题的正确答案
        let answer = questionForm.value.answer;
        let options = null;
        
        if (questionForm.value.type === 'SINGLE_CHOICE' || questionForm.value.type === 'MULTIPLE_CHOICE') {
          options = questionForm.value.options.map(option => ({
            label: option.label,
            content: option.content,
            isCorrect: option.isCorrect
          }));
          
          // 确保单选题只有一个正确答案
          if (questionForm.value.type === 'SINGLE_CHOICE') {
            const correctOptions = options.filter(o => o.isCorrect);
            if (correctOptions.length !== 1) {
              ElMessage.error('单选题必须有且只有一个正确答案');
              return;
            }
          }
          
          // 确保多选题至少有一个正确答案
          if (questionForm.value.type === 'MULTIPLE_CHOICE') {
            const correctOptions = options.filter(o => o.isCorrect);
            if (correctOptions.length === 0) {
              ElMessage.error('多选题至少需要一个正确答案');
              return;
            }
          }
        }
        
        const questionData = {
          type: questionForm.value.type,
          content: questionForm.value.content,
          score: questionForm.value.score,
          options,
          answer,
          referenceAnswer: questionForm.value.type === 'SHORT_ANSWER' ? questionForm.value.referenceAnswer : null,
          examId: examId.value
        };
        
        if (isEditQuestionMode.value && currentEditQuestion.value) {
          // 更新题目
          await questionApi.updateQuestion(currentEditQuestion.value.id, questionData);
          ElMessage.success('题目更新成功');
        } else {
          // 创建题目
          await questionApi.createQuestion(questionData);
          ElMessage.success('题目创建成功');
        }
        
        questionDialogVisible.value = false;
        fetchQuestions(); // 刷新题目列表
      } catch (error) {
        console.error('保存题目失败:', error);
        ElMessage.error('保存题目失败');
      }
    }
  });
};

// 删除题目
const deleteQuestion = async (question) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这道题目吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await questionApi.deleteQuestion(question.id);
    ElMessage.success('题目删除成功');
    fetchQuestions(); // 刷新题目列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题目失败:', error);
      ElMessage.error('删除题目失败');
    }
  }
};

// 查看考试记录
const viewExamRecords = async () => {
  recordsDialogVisible.value = true;
  await fetchExamRecords();
};

// 查看学生答卷
const viewStudentAnswers = (record) => {
  // 跳转到学生答卷详情页面
  router.push(`/exam/${examId.value}/record/${record.id}`);
};

// 初始化学生答案
const initStudentAnswers = () => {
  studentAnswers.value = questions.value.map(question => {
    let defaultAnswer;
    
    switch (question.type) {
      case 'SINGLE_CHOICE':
        defaultAnswer = '';
        break;
      case 'MULTIPLE_CHOICE':
        defaultAnswer = [];
        break;
      case 'TRUE_FALSE':
        defaultAnswer = '';
        break;
      case 'FILL_BLANK':
      case 'SHORT_ANSWER':
        defaultAnswer = '';
        break;
      default:
        defaultAnswer = '';
    }
    
    return {
      questionId: question.id,
      questionType: question.type,
      answer: defaultAnswer
    };
  });
};

// 开始考试计时器
const startExamTimer = () => {
  examStartTime.value = new Date();
  remainingTime.value = exam.value.duration * 60; // 转换为秒
  
  timerInterval.value = setInterval(() => {
    remainingTime.value -= 1;
    
    if (remainingTime.value <= 0) {
      // 时间到，自动提交
      clearInterval(timerInterval.value);
      submitExam();
    }
  }, 1000);
};

// 上一题
const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value -= 1;
  }
};

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value += 1;
  }
};

// 获取题目按钮类型
const getQuestionButtonType = (index) => {
  if (index === currentQuestionIndex.value) {
    return 'primary';
  }
  
  const answer = studentAnswers.value[index];
  if (!answer) return '';
  
  // 判断是否已答题
  let hasAnswer = false;
  
  switch (answer.questionType) {
    case 'SINGLE_CHOICE':
    case 'TRUE_FALSE':
    case 'FILL_BLANK':
    case 'SHORT_ANSWER':
      hasAnswer = !!answer.answer;
      break;
    case 'MULTIPLE_CHOICE':
      hasAnswer = Array.isArray(answer.answer) && answer.answer.length > 0;
      break;
  }
  
  return hasAnswer ? 'success' : '';
};

// 确认提交考试
const confirmSubmitExam = async () => {
  // 检查是否有未答题目
  const unansweredCount = studentAnswers.value.filter(answer => {
    if (answer.questionType === 'MULTIPLE_CHOICE') {
      return !Array.isArray(answer.answer) || answer.answer.length === 0;
    } else {
      return !answer.answer;
    }
  }).length;
  
  let message = '确定要提交考试吗？';
  if (unansweredCount > 0) {
    message = `您还有 ${unansweredCount} 道题目未作答，确定要提交吗？`;
  }
  
  try {
    await ElMessageBox.confirm(
      message,
      '提示',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '继续答题',
        type: 'warning',
      }
    );
    
    submitExam();
  } catch (error) {
    // 用户取消提交
  }
};

// 提交考试
const submitExam = async () => {
  try {
    // 停止计时器
    if (timerInterval.value) {
      clearInterval(timerInterval.value);
    }
    
    // 计算考试用时（秒）
    const endTime = new Date();
    const duration = Math.floor((endTime - examStartTime.value) / 1000);
    
    // 提交考试记录
    const submitData = {
      examId: examId.value,
      answers: studentAnswers.value,
      duration
    };
    
    const res = await examRecordApi.submitExamRecord(submitData);
    examRecord.value = res.data;
    examSubmitted.value = true;
    
    ElMessage.success('考试提交成功');
  } catch (error) {
    console.error('提交考试失败:', error);
    ElMessage.error('提交考试失败，请重试');
  }
};

// 获取题目类型文本
const getQuestionTypeText = (type) => {
  const typeMap = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'FILL_BLANK': '填空题',
    'SHORT_ANSWER': '简答题'
  };
  return typeMap[type] || '未知类型';
};

// 获取选项内容
const getOptionContent = (questionId, optionId) => {
  const question = questions.value.find(q => q.id === questionId);
  if (!question || !question.options) return '';
  
  const option = question.options.find(o => o.id === optionId);
  return option ? `${option.label}. ${option.content}` : '';
};

// 获取正确选项内容（单选题）
const getCorrectOptionContent = (questionId) => {
  const question = questions.value.find(q => q.id === questionId);
  if (!question || !question.options) return '';
  
  const correctOption = question.options.find(o => o.isCorrect);
  return correctOption ? `${correctOption.label}. ${correctOption.content}` : '';
};

// 获取正确选项（多选题）
const getCorrectOptions = (questionId) => {
  const question = questions.value.find(q => q.id === questionId);
  if (!question || !question.options) return [];
  
  return question.options.filter(o => o.isCorrect);
};

// 根据ID获取题目
const getQuestionById = (questionId) => {
  return questions.value.find(q => q.id === questionId) || {};
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '未设置';
  const date = new Date(dateString);
  return date.toLocaleString();
};

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '0秒';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;
  
  let result = '';
  if (hours > 0) result += `${hours}小时`;
  if (minutes > 0) result += `${minutes}分钟`;
  if (remainingSeconds > 0 || result === '') result += `${remainingSeconds}秒`;
  
  return result;
};

// 监听题目类型变化
watch(() => questionForm.value.type, (newType) => {
  // 重置答案
  questionForm.value.answer = '';
  questionForm.value.referenceAnswer = '';
  
  // 重置选项
  if (newType === 'SINGLE_CHOICE' || newType === 'MULTIPLE_CHOICE') {
    questionForm.value.options = [
      { label: 'A', content: '', isCorrect: false },
      { label: 'B', content: '', isCorrect: false },
      { label: 'C', content: '', isCorrect: false },
      { label: 'D', content: '', isCorrect: false }
    ];
  }
});

onMounted(async () => {
  await fetchExamInfo();
  
  if (isStudent.value && isStudentView.value) {
    // 检查是否已提交过考试
    await fetchMyExamRecord();
    
    if (!examSubmitted.value) {
      // 未提交过，获取题目开始考试
      await fetchQuestions();
    }
  } else if (isTeacher.value && !isStudentView.value) {
    // 教师管理考试
    await fetchQuestions();
  }
});

onBeforeUnmount(() => {
  // 清除计时器
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
});
</script>

<style scoped>
.exam-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 教师视图样式 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 16px;
  color: #666;
  margin: 0 0 15px 0;
}

.exam-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.questions-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.question-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background-color: #409EFF;
  color: white;
  border-radius: 50%;
  font-weight: bold;
}

.question-type-tag {
  background-color: #E6F1FC;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-score {
  color: #F56C6C;
  font-weight: bold;
}

.question-content {
  margin-bottom: 15px;
}

.question-text {
  font-size: 16px;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.question-options {
  margin-left: 20px;
}

.question-option {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
}

.option-label {
  font-weight: bold;
  margin-right: 8px;
  min-width: 20px;
}

.correct-option {
  color: #67C23A;
  font-weight: bold;
}

.correct-answer, .reference-answer {
  margin-top: 10px;
  color: #67C23A;
  font-weight: bold;
}

.option-form-item {
  margin-bottom: 15px;
}

.option-input-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 学生考试视图样式 */
.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.exam-info h1 {
  font-size: 24px;
  margin: 0 0 10px 0;
}

.exam-timer {
  text-align: center;
  padding: 10px 20px;
  background-color: #F56C6C;
  color: white;
  border-radius: 8px;
}

.timer-label {
  font-size: 14px;
  margin-bottom: 5px;
}

.timer-value {
  font-size: 24px;
  font-weight: bold;
}

.exam-content {
  display: flex;
  gap: 20px;
}

.questions-navigation {
  width: 200px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.navigation-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  text-align: center;
}

.navigation-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.submit-section {
  text-align: center;
  margin-top: 20px;
}

.question-container {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.current-question {
  margin-bottom: 20px;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

/* 考试结果视图样式 */
.result-header {
  text-align: center;
  margin-bottom: 30px;
}

.result-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.result-card {
  margin-bottom: 20px;
}

.result-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.result-score {
  display: flex;
  align-items: baseline;
  margin-bottom: 20px;
}

.score-value {
  font-size: 60px;
  font-weight: bold;
  color: #409EFF;
}

.score-total {
  font-size: 24px;
  color: #666;
  margin-left: 5px;
}

.result-status {
  margin-bottom: 20px;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.meta-label {
  color: #666;
}

.meta-value {
  font-weight: bold;
}

.answer-details {
  margin-top: 20px;
}

.answer-detail {
  padding: 10px;
}

.answer-content, .correct-answer {
  margin-top: 15px;
  display: flex;
}

.answer-label {
  font-weight: bold;
  width: 80px;
  flex-shrink: 0;
}

.result-actions {
  margin-top: 30px;
  text-align: center;
}

.dialog-footer {
  text-align: right;
  margin-top: 20px;
}
</style>