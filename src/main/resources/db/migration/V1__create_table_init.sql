CREATE SCHEMA IF NOT EXISTS gmc AUTHORIZATION gmc;

/** TT_TRADING_SETTING **/
CREATE TABLE IF NOT EXISTS tt_setting
(
    id            BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    setting_type  VARCHAR(3)   NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    created_by    VARCHAR(50)  NOT NULL,
    created_on    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  NOT NULL,
    updated_on    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tt_setting PRIMARY KEY (id),
    CONSTRAINT uk01_tt_setting UNIQUE (setting_type)
);

COMMENT ON TABLE tt_setting IS '트레이딩 설정';
COMMENT ON COLUMN tt_setting.id IS '설정 ID';
COMMENT ON COLUMN tt_setting.setting_type IS '설정 타입';
COMMENT ON COLUMN tt_setting.setting_value IS '설정 값';
COMMENT ON COLUMN tt_setting.created_by IS '등록자';
COMMENT ON COLUMN tt_setting.created_on IS '등록일시';
COMMENT ON COLUMN tt_setting.updated_by IS '수정자';
COMMENT ON COLUMN tt_setting.updated_on IS '수정일시';