package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 11/1/16.
 */
@Table("content")
public class AJEntityQuestion extends Model {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    // FIELDS
    public static final String ID = "id";
    public static final String COLLECTION_ID = "collection_id";
    public static final String MODIFIER_ID = "modifier_id";
    public static final String IS_DELETED = "is_deleted";
    public static final String QUESTION = "question";
    public static final String COURSE_ID = "course_id";
    public static final String CREATOR_ID = "creator_id";
    public static final String CONTENT_FORMAT = "content_format";
    public static final String CONTENT_SUBFORMAT = "content_subformat";
    public static final String TITLE = "title";
    public static final String LICENSE = "license";
    public static final String NARRATION = "narration";

    public static final String OPEN_ENDED_QUESTION_SUBFORMAT = "open_ended_question";

    // QUERIES & FILTERS
    public static final String FETCH_ASSESSMENT_GRADING =
        "SELECT question.id FROM content question, collection collection WHERE question.collection_id = collection.id "
            + " AND collection.format = 'assessment' AND question.content_subformat = 'open_ended_question' AND "
            + "question.content_format = 'question' " + " AND collection.grading = 'teacher' AND "
            + "question.is_deleted = 'false' AND collection.is_deleted = 'false' AND question.collection_id IS NOT "
            + "NULL AND question.collection_id = ?::uuid";

    public static final String IS_VALID_ASSESSMENT =
        "select count(id) from collection where id = ?::uuid and format = 'assessment'::content_container_type and "
            + "is_deleted = false";
    public static final String UPDATE_ASSESSMENT_GRADING =
        "UPDATE collection SET grading = 'system' WHERE id = ?::uuid AND is_deleted = 'false'";
    public static final String UPDATE_CONTAINER_TIMESTAMP =
        "update collection set updated_at = now() where id = ?::uuid and is_deleted = 'false'";
    public static final String OPEN_ENDED_QUESTION_FILTER =
        "collection_id = ?::uuid and content_subformat = 'open_ended_question'::content_subformat_type and "
            + "is_deleted = false";

    public static final String VALIDATE_EXISTS_NON_DELETED =
        "select id, creator_id, publish_date, collection_id, course_id, title, content_subformat from "
            + "content where content_format = " + "?::content_format_type and id = ?::uuid and is_deleted = ?";
    public static final String FETCH_QUESTION =
        "select id, title, publish_date, description, answer, metadata, taxonomy, "
            + "hint_explanation_detail, thumbnail, narration, "
            + "license, creator_id, content_subformat, visible_on_profile from "
            + "content where content_format = ?::content_format_type and id = ?::uuid and is_deleted = ?";
    public static final String AUTH_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?);";
    // TABLES
    public static final String TABLE_COURSE = "course";
    public static final String TABLE_QUESTION = "content";
    public static final String TABLE_COLLECTION = "collection";
    // FIELD LISTS
    public static final List<String> FETCH_QUESTION_FIELDS = Arrays.asList("id", "title", "publish_date",
        "description", "answer", "metadata", "taxonomy", "hint_explanation_detail", "thumbnail",
        "license", "creator_id", "content_subformat", "visible_on_profile", "narration");
    // What fields are allowed in request payload. Note this does not include
    // the auto populate fields
    public static final List<String> INSERT_QUESTION_ALLOWED_FIELDS =
        Arrays.asList("title", "description", "content_subformat", "answer", "metadata", "taxonomy",
            "narration", "hint_explanation_detail", "thumbnail", "visible_on_profile");
    public static final List<String> UPDATE_QUESTION_ALLOWED_FIELDS =
        Arrays.asList("title", "description", "answer", "metadata", "taxonomy",
            "hint_explanation_detail", "thumbnail", "visible_on_profile", "narration");
    public static final List<String> UPDATE_QUESTION_FORBIDDEN_FIELDS = Arrays.asList("id", "url", "created_at",
        "updated_at", "creator_id", "modifier_id", "original_creator_id", "original_content_id", "publish_date",
        "content_format", "content_subformat", "course_id", "unit_id", "lesson_id", "collection_id",
        "sequence_id", "is_copyright_owner", "copyright_owner", "info", "display_guide", "accessibility", "is_deleted");
    public static final List<String> INSERT_QUESTION_FORBIDDEN_FIELDS = Arrays.asList("id", "url", "created_at",
        "updated_at", "creator_id", "modifier_id", "original_creator_id", "original_content_id", "publish_date",
        "content_format", "course_id", "unit_id", "lesson_id", "collection_id", "sequence_id",
        "is_copyright_owner", "copyright_owner", "info", "display_guide", "accessibility", "is_deleted");
    public static final List<String> INSERT_QUESTION_MANDATORY_FIELDS = Arrays.asList("title", "description",
        "content_subformat");

    public static final List<String> QUESTION_TYPES =
        Arrays.asList("multiple_choice_question", "multiple_answer_question", "true_false_question",
            "fill_in_the_blank_question", "open_ended_question", "hot_text_reorder_question",
            "hot_text_highlight_question", "hot_spot_image_question", "hot_spot_text_question", "external_question");
    private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityQuestion.class);
    // TYPES
    private static final String UUID_TYPE = "uuid";
    private static final String JSONB_TYPE = "jsonb";
    private static final String CONTENT_FORMAT_TYPE = "content_format_type";
    private static final String CONTENT_SUBFORMAT_TYPE = "content_subformat_type";

    public void setModifierId(String modifier) {
        setPGObject(MODIFIER_ID, UUID_TYPE, modifier);
    }

    public void setCreatorId(String creatorId) {
        setPGObject(CREATOR_ID, UUID_TYPE, creatorId);
    }

    // NOTE:
    // We do not deal with nested objects, only first level ones
    // We do not check for forbidden fields, it should be done before this
    // The field should be present in fields array
    public void setAllFromJson(JsonObject input, List<String> fields) {
        input.getMap().forEach((s, o) -> {
            if (fields.contains(s)) {
                // Note that special UUID cases for modifier and creator should
                // be handled internally and not via map, so we do not care
                if (o instanceof JsonObject) {
                    setPGObject(s, JSONB_TYPE, o.toString());
                } else if (o instanceof JsonArray) {
                    setPGObject(s, JSONB_TYPE, o.toString());
                } else if ((s != null) && s.equalsIgnoreCase(CONTENT_SUBFORMAT)) {
                    setContentSubformatType((String) o);
                } else {
                    this.set(s, o);
                }
            }
        });
    }

    public void validateMandatoryFields() {
        for (String field : INSERT_QUESTION_MANDATORY_FIELDS) {
            Object value = this.get(field);
            if ((value == null) || value.toString().isEmpty()) {
                LOGGER.debug("Creation payload '{}' not allowed to have null or be empty");
                this.errors().put(field, RESOURCE_BUNDLE.getString("missing.mandatory.field"));
            }
        }
    }

    public void setContentFormatQuestion() {
        setPGObject(CONTENT_FORMAT, CONTENT_FORMAT_TYPE, QUESTION);
    }

    public void setLicense(Integer code) {
        this.set(LICENSE, code);
    }

    private void setContentSubformatType(String value) {
        if (!QUESTION_TYPES.contains(value)) {
            this.errors().put(CONTENT_SUBFORMAT, RESOURCE_BUNDLE.getString("invalid.value"));
        }
        setPGObject(CONTENT_SUBFORMAT, CONTENT_SUBFORMAT_TYPE, value);
    }

    private void setPGObject(String field, String type, String value) {
        PGobject pgObject = new PGobject();
        pgObject.setType(type);
        try {
            pgObject.setValue(value);
            this.set(field, pgObject);
        } catch (SQLException e) {
            LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
            this.errors().put(field, value);
        }
    }
}
