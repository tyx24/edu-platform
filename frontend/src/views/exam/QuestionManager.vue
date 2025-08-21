<template>
  <div class="question-manager-container">
    <div class="page-header">
      <h2>题目管理</h2>
      <p>{{ exam.title || '加载中...' }}</p>
    </div>

    <el-card class="info-card">
      <div class="exam-info">
        <div class="info-item">
          <span class="label">考试时间:</span>
          <span class="value">{{ formatDateTime(exam.startTime) }} - {{ formatDateTime(exam.endTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">考试时长:</span>
          <span class="value">{{ exam.duration }} 分钟</span>
        </div>
        <div class="info-item">
          <span class="label">总分:</span>
          <span class="value">{{ exam.totalScore }}</span>
        </div>
        <div class="info-item">
          <span class="label">及格分数:</span>
          <span class="value">{{ exam.passingScore }}</span>
        </div>
      </div>
    </el-card>

    <div class="action-bar">
      <el-button type="primary" @click="openQuestionDialog()">
        <el-icon><Plus /></el-icon> 添加题目
      </el-button>
      <el-button type="success" @click="importQuestions">
        <el-icon><Upload /></el-icon> 导入题目
      </el-button>
      <el-button @click="goBack">
        <el-icon><Back /></el-icon> 返回考试
      </el-button>
    </div>

    <div class="question-stats">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ questions.length }}</div>
            <div class="stat-label">题目总数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ getTotalScore() }}</div>
            <div class="stat-label">当前总分</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ getQuestionTypeCount('SINGLE_CHOICE') }}</div>
            <div class="stat-label">单选题</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ getQuestionTypeCount('MULTIPLE_CHOICE') }}</div>
            <div class="stat-label">多选题</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ getQuestionTypeCount('TRUE_FALSE') }}</div>
            <div class="stat-label">判断题</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ getQuestionTypeCount('FILL_BLANK') + getQuestionTypeCount('SHORT_ANSWER') }}</div>
            <div class="stat-label">填空/简答题</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="question-list">
      <el-empty v-if="questions.length === 0" description="暂无题目，请添加题目" />
      
      <div v-else>
        <el-card v-for="(question, index) in questions" :key="question.id" class="question-card">
          <div class="question-header">
            <div class="question-index">{{ index + 1 }}</div>
            <div class="question-type-tag">
              {{ getQuestionTypeText(question.type) }}
            </div>
            <div class="question-score">{{ question.score }} 分</div>
          </div>
          
          <div class="question-content">
            <div class="question-text">{{ question.content }}</div>
            
            <!-- 单选题和多选题选项 -->
            <div v-if="question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'" class="question-options">
              <div 
                v-for="option in question.options" 
                :key="option.id" 
                class="option-item"
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
          
          <div class="question-actions">
            <el-button type="primary" size="small" @click="editQuestion(question)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteQuestion(question)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 题目表单对话框 -->
    <el-dialog
      v-model="questionDialogVisible"
      :title="isEditQuestionMode ? '编辑题目' : '添加题目'"
      width="60%"
      destroy-on-close
    >
      <el-form ref="questionFormRef" :model="questionForm" :rules="questionRules" label-width="100px">
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="questionForm.type" placeholder="选择题目类型" style="width: 100%">
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
        
        <!-- 单选题和多选题选项 -->
        <template v-if="questionForm.type === 'SINGLE_CHOICE' || questionForm.type === 'MULTIPLE_CHOICE'">
          <el-divider content-position="left">选项</el-divider>
          
          <div 
            v-for="(option, index) in questionForm.options" 
            :key="index"
            class="option-form-item"
          >
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="18">
                <el-input 
                  v-model="option.content" 
                  :placeholder="`选项 ${option.label} 内容`"
                />
              </el-col>
              <el-col :span="4">
                <el-checkbox 
                  v-if="questionForm.type === 'SINGLE_CHOICE'"
                  v-model="option.isCorrect"
                  @change="handleSingleChoiceChange(index)"
                >
                  正确答案
                </el-checkbox>
                <el-checkbox 
                  v-else
                  v-model="option.isCorrect"
                >
                  正确答案
                </el-checkbox>
              </el-col>
              <el-col :span="2">
                <el-button 
                  type="danger" 
                  circle 
                  @click="removeOption(index)"
                  :disabled="questionForm.options.length <= 2"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-col>
            </el-row>
          </div>
          
          <div class="add-option-button">
            <el-button 
              type="primary" 
              @click="addOption"
              :disabled="questionForm.options.length >= 8"
            >
              <el-icon><Plus /></el-icon> 添加选项
            </el-button>
          </div>
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

    <!-- 导入题目对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入题目"
      width="50%"
    >
      <el-tabs v-model="importTab">
        <el-tab-pane label="从文件导入" name="file">
          <el-upload
            class="upload-demo"
            drag
            action="/api/question/import"
            :headers="{ 'X-Exam-Id': examId }"
            :on-success="handleImportSuccess"
            :on-error="handleImportError"
            accept=".xlsx,.xls,.csv"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持Excel或CSV格式，请下载<el-link type="primary" @click="downloadTemplate">导入模板</el-link>
              </div>
            </template>
          </el-upload>
        </el-tab-pane>
        <el-tab-pane label="从题库导入" name="bank">
          <el-form :inline="true" class="search-form">
            <el-form-item label="题目类型">
              <el-select v-model="bankSearchForm.type" placeholder="选择题目类型" clearable>
                <el-option label="全部" value="" />
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="TRUE_FALSE" />
                <el-option label="填空题" value="FILL_BLANK" />
                <el-option label="简答题" value="SHORT_ANSWER" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="bankSearchForm.keyword" placeholder="搜索题目内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchQuestionBank">搜索</el-button>
            </el-form-item>
          </el-form>
          
          <el-table
            :data="questionBank"
            style="width: 100%"
            @selection-change="handleBankSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                {{ getQuestionTypeText(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="content" label="题目内容" />
            <el-table-column prop="score" label="分值" width="80" />
          </el-table>
          
          <div class="bank-import-footer">
            <el-pagination
              v-model:current-page="bankPage.current"
              v-model:page-size="bankPage.size"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              :total="bankPage.total"
              @size-change="handleBankSizeChange"
              @current-change="handleBankCurrentChange"
            />
            
            <el-button 
              type="primary" 
              @click="importFromBank"
              :disabled="selectedBankQuestions.length === 0"
            >
              导入选中题目 ({{ selectedBankQuestions.length }})
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Back, Upload, UploadFilled } from '@element-plus/icons-vue';
import { examApi } from '../../api/exam';
import { questionApi } from '../../api/question';

const route = useRoute();
const router = useRouter();

// 考试ID
const examId = computed(() => route.params.examId);

// 考试信息
const exam = ref({});

// 题目列表
const questions = ref([]);

// 加载状态
const loading = ref(false);

// 题目对话框
const questionDialogVisible = ref(false);
const isEditQuestionMode = ref(false);
const currentEditQuestion = ref(null);
const questionFormRef = ref(null);

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

// 题目表单验证规则
const questionRules = {
  type: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  score: [
    { required: true, message: '请设置分值', trigger: 'change' },
    { type: 'number', min: 1, message: '分值必须大于0', trigger: 'change' }
  ],
  answer: [
    { 
      required: true, 
      message: '请选择正确答案', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if ((questionForm.value.type === 'TRUE_FALSE' || 
             questionForm.value.type === 'FILL_BLANK') && 
            !value) {
          callback(new Error('请选择/输入正确答案'));
        } else if (questionForm.value.type === 'SINGLE_CHOICE') {
          const hasCorrect = questionForm.value.options.some(option => option.isCorrect);
          if (!hasCorrect) {
            callback(new Error('请选择一个正确答案'));
          } else {
            callback();
          }
        } else if (questionForm.value.type === 'MULTIPLE_CHOICE') {
          const hasCorrect = questionForm.value.options.some(option => option.isCorrect);
          if (!hasCorrect) {
            callback(new Error('请至少选择一个正确答案'));
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

// 导入题目对话框
const importDialogVisible = ref(false);
const importTab = ref('file');

// 题库搜索表单
const bankSearchForm = ref({
  type: '',
  keyword: ''
});

// 题库分页
const bankPage = ref({
  current: 1,
  size: 10,
  total: 0
});

// 题库题目列表
const questionBank = ref([]);

// 选中的题库题目
const selectedBankQuestions = ref([]);

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchExamInfo();
  await fetchQuestions();
});

// 获取考试信息
const fetchExamInfo = async () => {
  loading.value = true;
  try {
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
    const res = await questionApi.getQuestionList(examId.value);
    questions.value = res.data || [];
  } catch (error) {
    console.error('获取题目列表失败:', error);
    ElMessage.error('获取题目列表失败');
  } finally {
    loading.value = false;
  }
};

// 打开题目对话框
const openQuestionDialog = () => {
  isEditQuestionMode.value = false;
  currentEditQuestion.value = null;
  
  // 重置表单
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

// 处理单选题选项变更
const handleSingleChoiceChange = (index) => {
  // 确保单选题只有一个正确答案
  questionForm.value.options.forEach((option, idx) => {
    if (idx !== index) {
      option.isCorrect = false;
    }
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
        }
        
        // 构建题目数据
        const questionData = {
          examId: examId.value,
          type: questionForm.value.type,
          content: questionForm.value.content,
          score: questionForm.value.score,
          options,
          answer,
          referenceAnswer: questionForm.value.referenceAnswer
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

// 打开导入题目对话框
const importQuestions = () => {
  importDialogVisible.value = true;
  importTab.value = 'file';
  bankSearchForm.value = { type: '', keyword: '' };
  bankPage.value = { current: 1, size: 10, total: 0 };
  questionBank.value = [];
  selectedBankQuestions.value = [];
};

// 下载导入模板
const downloadTemplate = () => {
  window.open('/api/question/template', '_blank');
};

// 处理导入成功
const handleImportSuccess = (res) => {
  ElMessage.success(`成功导入 ${res.data.count} 道题目`);
  importDialogVisible.value = false;
  fetchQuestions(); // 刷新题目列表
};

// 处理导入失败
const handleImportError = (error) => {
  console.error('导入题目失败:', error);
  ElMessage.error('导入题目失败');
};

// 搜索题库
const searchQuestionBank = async () => {
  try {
    const params = {
      ...bankSearchForm.value,
      page: bankPage.value.current,
      size: bankPage.value.size
    };
    
    const res = await questionApi.searchQuestionBank(params);
    questionBank.value = res.data.records || [];
    bankPage.value.total = res.data.total || 0;
  } catch (error) {
    console.error('搜索题库失败:', error);
    ElMessage.error('搜索题库失败');
  }
};

// 处理题库分页大小变更
const handleBankSizeChange = (size) => {
  bankPage.value.size = size;
  searchQuestionBank();
};

// 处理题库当前页变更
const handleBankCurrentChange = (current) => {
  bankPage.value.current = current;
  searchQuestionBank();
};

// 处理题库选择变更
const handleBankSelectionChange = (selection) => {
  selectedBankQuestions.value = selection;
};

// 从题库导入题目
const importFromBank = async () => {
  try {
    if (selectedBankQuestions.value.length === 0) {
      ElMessage.warning('请选择要导入的题目');
      return;
    }
    
    const questionIds = selectedBankQuestions.value.map(q => q.id);
    await questionApi.importFromBank(examId.value, { questionIds });
    
    ElMessage.success(`成功导入 ${questionIds.length} 道题目`);
    importDialogVisible.value = false;
    fetchQuestions(); // 刷新题目列表
  } catch (error) {
    console.error('从题库导入题目失败:', error);
    ElMessage.error('从题库导入题目失败');
  }
};

// 返回考试页面
const goBack = () => {
  router.push(`/exam/${examId.value}`);
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

// 获取特定类型题目的数量
const getQuestionTypeCount = (type) => {
  return questions.value.filter(q => q.type === type).length;
};

// 获取当前题目总分
const getTotalScore = () => {
  return questions.value.reduce((sum, q) => sum + q.score, 0);
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '未设置';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.question-manager-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.info-card {
  margin-bottom: 20px;
}

.exam-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  flex: 1;
  min-width: 200px;
}

.info-item .label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.question-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.question-card {
  margin-bottom: 20px;
  position: relative;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.question-index {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-right: 10px;
}

.question-type-tag {
  background-color: #e6f1fc;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 10px;
}

.question-score {
  color: #f56c6c;
  font-weight: bold;
}

.question-content {
  margin-bottom: 15px;
}

.question-text {
  font-size: 16px;
  margin-bottom: 10px;
}

.question-options {
  margin-left: 20px;
}

.option-item {
  margin-bottom: 8px;
  padding: 5px 10px;
  border-radius: 4px;
}

.correct-option {
  background-color: #f0f9eb;
  color: #67c23a;
}

.option-label {
  font-weight: bold;
  margin-right: 8px;
}

.correct-answer, .reference-answer {
  margin-top: 10px;
  color: #67c23a;
  font-weight: bold;
}

.question-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.option-form-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.add-option-button {
  margin-top: 10px;
  margin-bottom: 20px;
}

.bank-import-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>