TRUNCATE TABLE DEV_DEVICE;

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID)
VALUES ('09999', 'server', 'Store 09999 Server',
        '*', '*', '*', '*', '*', '*', '*', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '00145');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID)
VALUES ('09999-001','pos', 'Store 00145 Register 1',
        'N_AMERICA', 'US', 'OH', '100', 'REGULAR', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '09999');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('00100-001','pos', 'Store 100 Register 1',
        'N_AMERICA', 'US', 'OH', '100', 'REGULAR', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '100', '-4:00');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('00500-001','pos', 'Store 500 Register 1',
        'N_AMERICA', 'US', 'CA', '500', 'POPUP', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '500', '-4:00');
INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('00500-002','pos', 'Store 500 Register 2',
        'N_AMERICA', 'US', 'CA', '500', 'POPUP', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '500', '-4:00');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('00900-010','pos', 'Store 900 Register 10',
        'EAST_AFRICA', 'UG', '', '900', 'REGULAR', 'MOBILE_POS', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '900', '-4:00');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('00039-011','pos', 'Store 00039 Register 11',
        'EAST_AFRICA', 'UG', '', '00039', 'REGULAR', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '00039', '-4:00');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('05243-013','pos', 'Store 00039 Register 11',
        'EAST_AFRICA', 'UG', '', '00039', 'REGULAR', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '05243', '-4:00');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('00418-002','pos', 'Store 481 Register 2',
        'N_AMERICA', 'US', 'OH', '100', 'REGULAR', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '00418', '-4:00');

INSERT into DEV_DEVICE(DEVICE_ID, APP_ID, DESCRIPTION,
                       TAG_REGION, TAG_COUNTRY, TAG_STATE, TAG_STORE_NUMBER, TAG_STORE_TYPE, TAG_APP_PROFILE, TAG_PRICE_ZONE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY, BUSINESS_UNIT_ID, TIMEZONE_OFFSET)
VALUES ('11111-111', 'server', 'Store 481 Register 2',
        'N_AMERICA', 'US', 'OH', '100', 'REGULAR', 'WORKSTATION', 'Metl', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test', '09999', '-4:00');

TRUNCATE TABLE DEV_DEVICE_AUTH;

INSERT into DEV_DEVICE_AUTH(DEVICE_ID, AUTH_TOKEN, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY)
VALUES ('09999-001', '123456789', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test');

INSERT into DEV_DEVICE_AUTH(DEVICE_ID, AUTH_TOKEN, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY)
VALUES ('11111-111', '23897837', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test');

INSERT into DEV_DEVICE_AUTH(DEVICE_ID, AUTH_TOKEN, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY)
VALUES ('00100-001', '987654321', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test');

TRUNCATE TABLE DEV_DEVICE_PARAM;

INSERT into DEV_DEVICE_PARAM(DEVICE_ID, APP_ID, PARAM_NAME, PARAM_VALUE, CREATE_TIME, CREATE_BY, LAST_UPDATE_TIME, LAST_UPDATE_BY)
VALUES ('00100-001', 'pos', 'brandId', 'default', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, 'test');
