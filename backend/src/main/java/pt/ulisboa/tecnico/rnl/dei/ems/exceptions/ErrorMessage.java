package pt.ulisboa.tecnico.rnl.dei.ems.exceptions;

public enum ErrorMessage {

	NO_SUCH_PERSON("Não existe nenhuma pessoa com o ID %s", 1001),
	PERSON_NAME_NOT_VALID("O nome da pessoa especificado não é válido.", 1002),
	PERSON_ALREADY_EXISTS("Já existe uma pessoa com o ID %s", 1003),
	PERSON_EMAIL_NOT_VALID("O email especificado não é válido.", 1004),
	PERSON_PASSWORD_NOT_VALID("A palavra-passe especificada não é válida.", 1005),
	EMAIL_ALREADY_EXISTS("Já existe uma conta com o email %s", 1006),

	INVALID_CREDENTIALS("Email ou palavra-passe inválidos.", 2001),
	CANNOT_IMPERSONATE_SELF("Não é possível personificar a própria conta.", 2002),
	NOT_AUTHENTICATED("Não autenticado.", 2003),
	NOT_IMPERSONATING("Não está a personificar nenhuma conta.", 2004),
	ACCESS_DENIED("Não tem permissões para executar esta ação.", 2005),

	NO_SUCH_SCHOOL("Não existe nenhuma escola com o ID %s", 3001),
	SCHOOL_NAME_NOT_VALID("O nome da escola especificado não é válido.", 3002),
	SCHOOL_CODE_NOT_VALID("O código da escola especificado não é válido.", 3003),
	SCHOOL_CODE_ALREADY_EXISTS("Já existe uma escola com o código %s", 3004),
	SCHOOL_REGION_NOT_VALID("A região da escola especificada não é válida.", 3005),

	NO_SUCH_DISCIPLINE("Não existe nenhuma disciplina com o ID %s", 4001),
	DISCIPLINE_NAME_NOT_VALID("O nome da disciplina especificado não é válido.", 4002),
	DISCIPLINE_CODE_NOT_VALID("O código da disciplina especificado não é válido.", 4003),
	DISCIPLINE_CODE_ALREADY_EXISTS("Já existe uma disciplina com o código %s", 4004),

	NO_SUCH_EXAM("Não existe nenhum exame com o ID %s", 5001),
	EXAM_TITLE_NOT_VALID("O título do exame não é válido.", 5002),
	EXAM_PDF_REQUIRED("O ficheiro PDF do exame é obrigatório.", 5003),
	EXAM_PDF_INVALID("O ficheiro enviado não é um PDF válido.", 5004),
	EXAM_ALREADY_SEGMENTED("O exame já se encontra segmentado.", 5005),
	EXAM_NOT_SEGMENTED("O exame ainda não foi segmentado.", 5006),
	EXAM_NO_QUESTIONS("O exame deve conter pelo menos uma pergunta para concluir a segmentação.", 5007),
	EXAM_STUDENT_NOT_VALID("O aluno associado ao exame não é válido.", 5008),
	EXAM_CANNOT_DELETE("Não é possível eliminar um exame neste estado.", 5009),
	EXAM_TOTAL_SCORE_NOT_20("A cotação total do exame deve ser exatamente 20.0 valores para concluir a separação. Cotação atual: %s", 5010),
	EXAM_SCORE_EXCEEDS_20("A soma das cotações não pode exceder 20.0 valores. Cotação atual: %s, valor a adicionar: %s", 5011),
	EXAMS_LOCKED_AFTER_DISTRIBUTION("As submissões e edições de exames estão bloqueadas permanentemente após a distribuição das provas.", 5012),
	EXAM_ALREADY_DISTRIBUTED("Não é possível alterar uma prova que já foi distribuída ou avaliada.", 5013),
	STUDENT_ALREADY_HAS_EXAM_FOR_DISCIPLINE("Este aluno já tem um exame associado a esta disciplina (%s).", 5014),
	CANNOT_DISTRIBUTE_UNSEGMENTED_EXAMS_EXIST("Não é possível distribuir tarefas enquanto existirem exames digitalizados pendentes de separação por itens (%s pendente(s)).", 5015),

	NO_SUCH_QUESTION("Não existe nenhuma pergunta com o ID %s", 6001),
	QUESTION_NUMBER_NOT_VALID("O número/identificador da pergunta não é válido.", 6002),
	QUESTION_MAX_SCORE_NOT_VALID("A cotação máxima da pergunta deve ser superior a zero.", 6003),
	QUESTION_PAGE_INVALID("A página selecionada para a pergunta não é válida.", 6004),
	QUESTION_CROP_INVALID("As coordenadas de recorte da pergunta são inválidas.", 6005),

	FILE_STORAGE_FAILED("Falha ao gravar o ficheiro no armazenamento: %s", 7001),
	FILE_NOT_FOUND("Ficheiro não encontrado: %s", 7002),
	PDF_PROCESSING_FAILED("Falha ao processar o ficheiro PDF: %s", 7003),

	NO_TEACHERS_FOR_DISCIPLINE("Não existem professores registados para a disciplina %s.", 8001),
	NO_QUESTIONS_TO_DISTRIBUTE("Não existem perguntas segmentadas pendentes de distribuição.", 8002),
	TASK_NOT_ASSIGNED_TO_TEACHER("Esta tarefa de avaliação não está atribuída a este professor.", 8003),
	INVALID_SCORE_RANGE("A nota atribuída deve estar entre 0 e a cotação máxima (%s valores).", 8004),
	EXAM_NOT_CORRECTED("O exame ainda não se encontra totalmente corrigido.", 8005),
	EXAM_NOT_RELEASED("A prova ainda não foi disponibilizada para consulta.", 8006),
	REVIEW_NOT_PERMITTED("O pedido de revisão não é permitido para este exame.", 8007),
	REVIEW_DEADLINE_EXPIRED("O prazo limite para submeter pedidos de revisão já expirou.", 8008),
	REVIEW_ALREADY_REQUESTED("Já existe um pedido de revisão para esta pergunta.", 8009),
	REVIEW_JUSTIFICATION_REQUIRED("A justificação do pedido de revisão é obrigatória.", 8010),
	NO_SUCH_REVIEW_REQUEST("Não existe nenhum pedido de revisão com o ID %s", 8011),
	REVIEW_NOT_ASSIGNED_TO_TEACHER("Este pedido de revisão não está atribuído a este professor.", 8012),
	REVIEWS_STILL_PENDING("Existem pedidos de revisão que ainda não foram avaliados pelos professores (%s pendente(s)).", 8013);


	private final String label;
	private final int code;

	ErrorMessage(String label, int code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public int getCode() {
		return this.code;
	}
}
