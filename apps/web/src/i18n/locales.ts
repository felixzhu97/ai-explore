export type Language = 'en' | 'zh' | 'ja' | 'fr' | 'es';

export interface Translations {
  // Navigation
  nav: {
    visionAI: string;
    documentQA: string;
    supervisor: string;
    kubernetes: string;
    monitoring: string;
    model: string;
    llmops: string;
    aiops: string;
    vectordb: string;
  };
  
  // Image Uploader
  imageUploader: {
    imageLabel: string;
    resultLabel: string;
    dropText: string;
    dropHint: string;
    analyzing: string;
    startAnalyze: string;
    uploadToAnalyze: string;
    selectImageError: string;
    requestFailed: string;
    processingFailed: string;
    clearImage: string;
    
    // Task tabs
    caption: string;
    detect: string;
    ocr: string;
    
    // Results
    noImageYet: string;
  };
  
  // RAG Chat
  ragChat: {
    title: string;
    modelBadge: string;
    uploadDocs: string;
    upload: string;
    askQuestion: string;
    inputPlaceholder: string;
    thinking: string;
    errorMessage: string;
    sources: string;
    similarity: string;
    
    // Quick questions
    whatIsThis: string;
    summarize: string;
    keyInfo: string;
    explain: string;
    
    // Documents section
    documents: string;
    noDocuments: string;
    selectedDocuments: string;
    selectAll: string;
    clearSelection: string;
    
    // Upload feedback
    filesSelected: string;
    uploadSuccess: string;
    uploadFailed: string;
    uploading: string;
    
    // Source badges
    basedOn: string;
  };

  // Agents
  agents: {
    startConversation: string;
    inputPlaceholder: string;
    thinking: string;
    errorMessage: string;
    descriptions: {
      supervisor: string;
      k8s: string;
      monitoring: string;
      model: string;
      llmops: string;
      aiops: string;
      vectordb: string;
    };
  };
}

export const translations: Record<Language, Translations> = {
  en: {
    nav: {
      visionAI: 'Vision AI',
      documentQA: 'Document QA',
      supervisor: 'Supervisor',
      kubernetes: 'K8s',
      monitoring: 'Monitoring',
      model: 'Models',
      llmops: 'LLMOps',
      aiops: 'AIOps',
      vectordb: 'VectorDB',
    },
    imageUploader: {
      imageLabel: 'Image',
      resultLabel: 'Analysis Result',
      dropText: 'Drag & drop or click to upload',
      dropHint: 'JPG, PNG, GIF, WebP',
      analyzing: 'Analyzing...',
      startAnalyze: 'Analyze',
      uploadToAnalyze: 'Upload an image to analyze',
      selectImageError: 'Please select an image file',
      requestFailed: 'Request failed',
      processingFailed: 'Processing failed',
      clearImage: 'Clear image',
      
      caption: 'Caption',
      detect: 'Detect',
      ocr: 'OCR',
      
      noImageYet: 'Upload an image to get started',
    },
    ragChat: {
      title: 'Document Q&A',
      modelBadge: 'RAG + GPT-4o-mini',
      uploadDocs: 'Upload Docs',
      upload: 'Upload',
      askQuestion: 'Ask me anything about your documents',
      inputPlaceholder: 'Type your question...',
      thinking: 'Thinking...',
      errorMessage: 'Sorry, an error occurred. Please ensure the RAG service is running.',
      sources: 'Sources',
      similarity: 'Similarity',
      
      whatIsThis: 'What is this about?',
      summarize: 'Summarize the key points',
      keyInfo: 'What are the key details?',
      explain: 'Explain the concepts',
      
      documents: 'Available Documents',
      noDocuments: 'No documents uploaded yet',
      selectedDocuments: 'Selected: {count}',
      selectAll: 'Select All',
      clearSelection: 'Clear',
      
      filesSelected: '{count} file(s) selected',
      uploadSuccess: '{name} uploaded successfully',
      uploadFailed: 'Failed to upload {name}',
      uploading: 'Uploading...',
      
      basedOn: 'Based on {count} source(s)',
    },
    agents: {
      startConversation: 'Start a conversation with the agent',
      inputPlaceholder: 'Type your message...',
      thinking: 'Thinking...',
      errorMessage: 'An error occurred. Please try again.',
      descriptions: {
        supervisor: 'Multi-agent orchestrator - coordinates specialized agents for complex tasks',
        k8s: 'Manage Kubernetes clusters, pods, services, and deployments',
        monitoring: 'Query metrics, set up alerts, and analyze system performance',
        model: 'Manage ML models, versioning, deployment, and inference',
        llmops: 'Train, evaluate, and fine-tune LLM models',
        aiops: 'Intelligent operations - incident analysis, root cause, automation',
        vectordb: 'Manage vector embeddings, similarity search, and document indexing',
      },
    },
  },
  zh: {
    nav: {
      visionAI: 'Vision AI',
      documentQA: '文档问答',
      supervisor: '协调器',
      kubernetes: 'K8s',
      monitoring: '监控',
      model: '模型',
      llmops: 'LLMOps',
      aiops: 'AIOps',
      vectordb: '向量库',
    },
    imageUploader: {
      imageLabel: '图片',
      resultLabel: '分析结果',
      dropText: '拖拽图片或点击上传',
      dropHint: 'JPG, PNG, GIF, WebP',
      analyzing: '分析中...',
      startAnalyze: '开始分析',
      uploadToAnalyze: '上传图片开始分析',
      selectImageError: '请选择图片文件',
      requestFailed: '请求失败',
      processingFailed: '处理失败',
      clearImage: '清除图片',
      
      caption: '图像描述',
      detect: '目标检测',
      ocr: '文字识别',
      
      noImageYet: '上传图片开始分析',
    },
    ragChat: {
      title: '文档问答',
      modelBadge: 'RAG + GPT-4o-mini',
      uploadDocs: '上传文档',
      upload: '上传',
      askQuestion: '开始问我关于文档的问题吧',
      inputPlaceholder: '输入问题...',
      thinking: '思考中...',
      errorMessage: '抱歉，发生了错误。请确保RAG服务正在运行。',
      sources: '参考来源',
      similarity: '相似度',
      
      whatIsThis: '这是什么内容？',
      summarize: '总结一下要点',
      keyInfo: '有哪些关键信息？',
      explain: '解释一下相关概念',
      
      documents: '可用文档',
      noDocuments: '暂无上传的文档',
      selectedDocuments: '已选择: {count}',
      selectAll: '全选',
      clearSelection: '清除',
      
      filesSelected: '已选择 {count} 个文件',
      uploadSuccess: '{name} 上传成功',
      uploadFailed: '{name} 上传失败',
      uploading: '上传中...',
      
      basedOn: '基于 {count} 个来源',
    },
    agents: {
      startConversation: '开始与 Agent 对话',
      inputPlaceholder: '输入消息...',
      thinking: '思考中...',
      errorMessage: '发生错误，请重试。',
      descriptions: {
        supervisor: '多 Agent 协调器 - 协调专业 Agent 处理复杂任务',
        k8s: '管理 Kubernetes 集群、Pod、服务和部署',
        monitoring: '查询指标、设置告警、分析系统性能',
        model: '管理 ML 模型、版本控制、部署和推理',
        llmops: '训练、评估和微调 LLM 模型',
        aiops: '智能运维 - 事件分析、根因定位、自动化',
        vectordb: '管理向量嵌入、相似度搜索和文档索引',
      },
    },
  },
  ja: {
    nav: {
      visionAI: 'Vision AI',
      documentQA: 'ドキュメント',
      supervisor: 'Supervisor',
      kubernetes: 'K8s',
      monitoring: '監視',
      model: 'モデル',
      llmops: 'LLMOps',
      aiops: 'AIOps',
      vectordb: 'VectorDB',
    },
    imageUploader: {
      imageLabel: '画像',
      resultLabel: '分析結果',
      dropText: 'ドラッグ＆ドロップまたはクリック',
      dropHint: 'JPG, PNG, GIF, WebP',
      analyzing: '分析中...',
      startAnalyze: '分析開始',
      uploadToAnalyze: '画像をアップロードして分析',
      selectImageError: '画像ファイルを選択してください',
      requestFailed: 'リクエスト失敗',
      processingFailed: '処理失敗',
      clearImage: '画像をクリア',
      
      caption: '画像説明',
      detect: '検出',
      ocr: '文字認識',
      
      noImageYet: '画像をアップロードして分析',
    },
    ragChat: {
      title: 'ドキュメント Q&A',
      modelBadge: 'RAG + GPT-4o-mini',
      uploadDocs: 'ドキュメント',
      upload: 'アップロード',
      askQuestion: 'ドキュメントについて質問してください',
      inputPlaceholder: '質問を入力...',
      thinking: '考え中...',
      errorMessage: 'エラーが発生しました。RAGサービスが実行されていることを確認してください。',
      sources: '参照元',
      similarity: '類似度',
      
      whatIsThis: 'これは何ですか？',
      summarize: '要点をまとめて',
      keyInfo: '重要な情報は？',
      explain: '概念を説明して',
      
      documents: '利用可能なドキュメント',
      noDocuments: 'アップロードされたドキュメントはありません',
      selectedDocuments: '選択中: {count}',
      selectAll: '全て選択',
      clearSelection: 'クリア',
      
      filesSelected: '{count} ファイル選択済み',
      uploadSuccess: '{name} アップロード成功',
      uploadFailed: '{name} アップロード失敗',
      uploading: 'アップロード中...',
      
      basedOn: '{count} ソースに基づく',
    },
    agents: {
      startConversation: 'エージェントと会話を始める',
      inputPlaceholder: 'メッセージを入力...',
      thinking: '考え中...',
      errorMessage: 'エラーが発生しました。もう一度お試しください。',
      descriptions: {
        supervisor: 'マルチエージェントオーケストレーター - 専門エージェントを調整して複雑なタスクを解決',
        k8s: 'Kubernetesクラスター、Pod、サービス、デプロイメントを管理',
        monitoring: 'メトリクス、クエリ、告警設定、システムパフォーマンス分析',
        model: 'MLモデルの管理、バージョン管理、デプロイ、推論',
        llmops: 'LLMモデルの訓練、評価、微調整',
        aiops: 'インテリジェントオペレーション - インシデント分析、根本原因分析、自動化',
        vectordb: 'ベクトル埋め込み、類似性検索、ドキュメントインデックス管理',
      },
    },
  },
  fr: {
    nav: {
      visionAI: 'Vision AI',
      documentQA: 'Documents',
      supervisor: 'Supervisor',
      kubernetes: 'K8s',
      monitoring: 'Surveillance',
      model: 'Modèles',
      llmops: 'LLMOps',
      aiops: 'AIOps',
      vectordb: 'VectorDB',
    },
    imageUploader: {
      imageLabel: 'Image',
      resultLabel: "Résultat d'analyse",
      dropText: 'Glisser-déposer ou cliquer',
      dropHint: 'JPG, PNG, GIF, WebP',
      analyzing: "Analyse en cours...",
      startAnalyze: "Lancer l'analyse",
      uploadToAnalyze: 'Télécharger une image à analyser',
      selectImageError: 'Veuillez sélectionner un fichier image',
      requestFailed: 'Échec de la requête',
      processingFailed: 'Échec du traitement',
      clearImage: "Effacer l'image",
      
      caption: 'Description',
      detect: 'Détection',
      ocr: 'OCR',
      
      noImageYet: "Téléchargez une image pour commencer",
    },
    ragChat: {
      title: "Q&R Documents",
      modelBadge: 'RAG + GPT-4o-mini',
      uploadDocs: 'Documents',
      upload: 'Téléverser',
      askQuestion: 'Posez-moi des questions sur vos documents',
      inputPlaceholder: 'Tapez votre question...',
      thinking: 'Réflexion...',
      errorMessage: "Désolé, une erreur s'est produite. Assurez-vous que le service RAG fonctionne.",
      sources: 'Sources',
      similarity: 'Similarité',
      
      whatIsThis: 'De quoi s agit-il ?',
      summarize: 'Résumez les points clés',
      keyInfo: 'Quelles sont les informations clés ?',
      explain: 'Expliquez les concepts',
      
      documents: 'Documents disponibles',
      noDocuments: 'Aucun document téléchargé',
      selectedDocuments: 'Sélectionnés: {count}',
      selectAll: 'Tout sélectionner',
      clearSelection: 'Effacer',
      
      filesSelected: '{count} fichier(s) sélectionné(s)',
      uploadSuccess: '{name} téléchargé avec succès',
      uploadFailed: 'Échec du téléchargement de {name}',
      uploading: 'Téléversement...',
      
      basedOn: 'Basé sur {count} source(s)',
    },
    agents: {
      startConversation: 'Démarrer une conversation avec l\'agent',
      inputPlaceholder: 'Tapez votre message...',
      thinking: 'Réflexion...',
      errorMessage: 'Une erreur s\'est produite. Veuillez réessayer.',
      descriptions: {
        supervisor: 'Orchestrateur multi-agents - coordonne les agents spécialisés pour les tâches complexes',
        k8s: 'Gérer les clusters Kubernetes, pods, services et déploiements',
        monitoring: 'Requêter les métriques, configurer les alertes et analyser les performances',
        model: 'Gérer les modèles ML, versioning, déploiement et inférence',
        llmops: 'Entraîner, évaluer et affiner les modèles LLM',
        aiops: 'Opérations intelligentes - analyse des incidents, cause racine, automatisation',
        vectordb: 'Gérer les embeddings vectoriels, recherche de similarité et indexation',
      },
    },
  },
  es: {
    nav: {
      visionAI: 'Vision AI',
      documentQA: 'Documentos',
      supervisor: 'Supervisor',
      kubernetes: 'K8s',
      monitoring: 'Monitoreo',
      model: 'Modelos',
      llmops: 'LLMOps',
      aiops: 'AIOps',
      vectordb: 'VectorDB',
    },
    imageUploader: {
      imageLabel: 'Imagen',
      resultLabel: 'Resultado',
      dropText: 'Arrastrar o hacer clic',
      dropHint: 'JPG, PNG, GIF, WebP',
      analyzing: 'Analizando...',
      startAnalyze: 'Analizar',
      uploadToAnalyze: 'Subir imagen para analizar',
      selectImageError: 'Seleccione un archivo de imagen',
      requestFailed: 'Solicitud fallida',
      processingFailed: 'Procesamiento fallido',
      clearImage: 'Borrar imagen',
      
      caption: 'Descripción',
      detect: 'Detección',
      ocr: 'OCR',
      
      noImageYet: 'Suba una imagen para comenzar',
    },
    ragChat: {
      title: 'Q&A Documentos',
      modelBadge: 'RAG + GPT-4o-mini',
      uploadDocs: 'Documentos',
      upload: 'Subir',
      askQuestion: 'Pregúntame sobre tus documentos',
      inputPlaceholder: 'Escribe tu pregunta...',
      thinking: 'Pensando...',
      errorMessage: 'Lo sentimos, occurred un error. Asegúrese de que el servicio RAG está funcionando.',
      sources: 'Fuentes',
      similarity: 'Similitud',
      
      whatIsThis: '¿De qué trata esto?',
      summarize: 'Resume los puntos clave',
      keyInfo: '¿Cuáles son los detalles clave?',
      explain: 'Explica los conceptos',
      
      documents: 'Documentos disponibles',
      noDocuments: 'No hay documentos subidos',
      selectedDocuments: 'Seleccionados: {count}',
      selectAll: 'Seleccionar todo',
      clearSelection: 'Limpiar',
      
      filesSelected: '{count} archivo(s) seleccionado(s)',
      uploadSuccess: '{name} subido correctamente',
      uploadFailed: 'Error al subir {name}',
      uploading: 'Subiendo...',
      
      basedOn: 'Basado en {count} fuente(s)',
    },
    agents: {
      startConversation: 'Iniciar una conversación con el agente',
      inputPlaceholder: 'Escribe tu mensaje...',
      thinking: 'Pensando...',
      errorMessage: 'Ocurrió un error. Por favor, inténtalo de nuevo.',
      descriptions: {
        supervisor: 'Orquestador multi-agente - coordina agentes especializados para tareas complejas',
        k8s: 'Gestionar clusters Kubernetes, pods, servicios y despliegues',
        monitoring: 'Consultar métricas, configurar alertas y analizar rendimiento del sistema',
        model: 'Gestionar modelos ML, versionado, despliegue e inferencia',
        llmops: 'Entrenar, evaluar y ajustar modelos LLM',
        aiops: 'Operaciones inteligentes - análisis de incidentes, causa raíz, automatización',
        vectordb: 'Gestionar embeddings vectoriales, búsqueda de similitud e indexación',
      },
    },
  },
};

export const languageNames: Record<Language, string> = {
  en: 'English',
  zh: '中文',
  ja: '日本語',
  fr: 'Français',
  es: 'Español',
};

export const languageFlags: Record<Language, string> = {
  en: '🇺🇸',
  zh: '🇨🇳',
  ja: '🇯🇵',
  fr: '🇫🇷',
  es: '🇪🇸',
};
