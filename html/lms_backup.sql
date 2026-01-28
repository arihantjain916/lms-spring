--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ai_chat_bot; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.ai_chat_bot (
    id character varying(255) NOT NULL,
    completion_tokens integer,
    conversation_id character varying(255),
    cost numeric(38,2),
    course_id character varying(255),
    created_at timestamp(6) with time zone,
    feedback character varying(255),
    flagged boolean,
    helpful boolean,
    lesson_id character varying(255),
    max_tokens integer,
    message_order integer,
    model character varying(255) NOT NULL,
    page character varying(255),
    prompt text NOT NULL,
    prompt_tokens integer,
    response text,
    role character varying(255),
    session_id character varying(255),
    temperature double precision,
    total_tokens integer,
    user_id character varying(255),
    CONSTRAINT ai_chat_bot_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ASSISTANT'::character varying, 'SYSTEM'::character varying])::text[])))
);


ALTER TABLE public.ai_chat_bot OWNER TO arihant;

--
-- Name: ai_daily_usage; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.ai_daily_usage (
    id character varying(255) NOT NULL,
    cost_used numeric(38,2),
    daily_cost_limit numeric(38,2),
    daily_question_limit integer,
    daily_token_limit integer,
    last_updated timestamp(6) with time zone,
    questions_used integer,
    tokens_used integer,
    usage_date date NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.ai_daily_usage OWNER TO arihant;

--
-- Name: asset; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.asset (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    name character varying(255),
    public_id character varying(255),
    type character varying(255),
    updated_at timestamp(6) without time zone,
    url character varying(255),
    user_id character varying(255)
);


ALTER TABLE public.asset OWNER TO arihant;

--
-- Name: asset_meta; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.asset_meta (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255),
    extension character varying(255),
    mime_type character varying(255),
    size bigint,
    updated_at timestamp(6) without time zone,
    visibility character varying(255),
    asset_id character varying(255) NOT NULL
);


ALTER TABLE public.asset_meta OWNER TO arihant;

--
-- Name: blog; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.blog (
    id character varying(255) NOT NULL,
    tag character varying(255) NOT NULL,
    category smallint NOT NULL,
    content character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255) NOT NULL,
    image_url character varying(255) NOT NULL,
    read_time character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    status smallint NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id character varying(255),
    is_featured boolean,
    CONSTRAINT blog_category_check CHECK (((category >= 0) AND (category <= 4))),
    CONSTRAINT blog_status_check CHECK (((status >= 0) AND (status <= 2)))
);


ALTER TABLE public.blog OWNER TO arihant;

--
-- Name: blog_comment; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.blog_comment (
    id character varying(255) NOT NULL,
    comment character varying(1000) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    blog_id character varying(255) NOT NULL,
    parent_id character varying(255),
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.blog_comment OWNER TO arihant;

--
-- Name: category; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.category (
    id character varying(255) NOT NULL,
    description character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    icon character varying(255),
    is_featured boolean
);


ALTER TABLE public.category OWNER TO arihant;

--
-- Name: contact_us; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.contact_us (
    id character varying(255) NOT NULL,
    department smallint,
    email character varying(255),
    message character varying(255),
    name character varying(255),
    phone character varying(255),
    subject character varying(255),
    CONSTRAINT contact_us_department_check CHECK (((department >= 0) AND (department <= 3)))
);


ALTER TABLE public.contact_us OWNER TO arihant;

--
-- Name: course_views; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.course_views (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    user_id character varying(255) NOT NULL,
    course_id bigint NOT NULL,
    ip character varying(255),
    referrer character varying(255),
    user_agent character varying(255)
);


ALTER TABLE public.course_views OWNER TO arihant;

--
-- Name: course_views_seq; Type: SEQUENCE; Schema: public; Owner: arihant
--

CREATE SEQUENCE public.course_views_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.course_views_seq OWNER TO arihant;

--
-- Name: courses; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.courses (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(255),
    slug character varying(255),
    title character varying(255),
    updated_at timestamp(6) without time zone NOT NULL,
    category_id character varying(255),
    instructor_id character varying(255),
    user_id character varying(255) NOT NULL,
    is_featured boolean,
    pricing_plan_id character varying(255)
);


ALTER TABLE public.courses OWNER TO arihant;

--
-- Name: courses_seq; Type: SEQUENCE; Schema: public; Owner: arihant
--

CREATE SEQUENCE public.courses_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.courses_seq OWNER TO arihant;

--
-- Name: enrollment; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.enrollment (
    id character varying(255) NOT NULL,
    enrolled_at timestamp(6) with time zone,
    course_id bigint,
    user_id character varying(255)
);


ALTER TABLE public.enrollment OWNER TO arihant;

--
-- Name: exam; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.exam (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    ends_at timestamp(6) with time zone NOT NULL,
    max_attempts integer,
    pass_marks integer NOT NULL,
    show_score_immediately boolean NOT NULL,
    shuffle_questions boolean NOT NULL,
    starts_at timestamp(6) with time zone NOT NULL,
    status smallint NOT NULL,
    time_limit_min integer NOT NULL,
    title character varying(255) NOT NULL,
    total_marks integer NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    course_id bigint NOT NULL,
    user_id character varying(255) NOT NULL,
    CONSTRAINT exam_status_check CHECK (((status >= 0) AND (status <= 3)))
);


ALTER TABLE public.exam OWNER TO arihant;

--
-- Name: exam_attempt; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.exam_attempt (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_attempt boolean,
    is_completed boolean,
    question_attempts bytea,
    updated_at timestamp(6) without time zone NOT NULL,
    exam_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.exam_attempt OWNER TO arihant;

--
-- Name: lesson; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.lesson (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(1000) NOT NULL,
    status character varying(1000) NOT NULL,
    thumbnail_url character varying(1000) NOT NULL,
    "time" character varying(1000) NOT NULL,
    title character varying(1000) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    video_url character varying(1000) NOT NULL,
    course_id bigint
);


ALTER TABLE public.lesson OWNER TO arihant;

--
-- Name: payments; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.payments (
    id character varying(255) NOT NULL,
    amount double precision,
    created_at timestamp(6) without time zone NOT NULL,
    currency character varying(255),
    status character varying(255),
    updated_at timestamp(6) without time zone NOT NULL,
    course_id bigint,
    pricing_plan_id character varying(255),
    user_id character varying(255),
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PAID'::character varying, 'FAILED'::character varying])::text[])))
);


ALTER TABLE public.payments OWNER TO arihant;

--
-- Name: pricing_plans; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.pricing_plans (
    id character varying(255) NOT NULL,
    currency character varying(255) NOT NULL,
    description character varying(255) NOT NULL,
    plan_type smallint NOT NULL,
    price double precision NOT NULL,
    title character varying(255) NOT NULL,
    course_id bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    CONSTRAINT pricing_plans_plan_type_check CHECK (((plan_type >= 0) AND (plan_type <= 3)))
);


ALTER TABLE public.pricing_plans OWNER TO arihant;

--
-- Name: question_attempt; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.question_attempt (
    id character varying(255) NOT NULL,
    answer character varying(255) NOT NULL,
    attempted_at timestamp(6) without time zone NOT NULL,
    exam_id character varying(255) NOT NULL,
    question_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.question_attempt OWNER TO arihant;

--
-- Name: question_options; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.question_options (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_correct boolean NOT NULL,
    option character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    question_id character varying(255) NOT NULL
);


ALTER TABLE public.question_options OWNER TO arihant;

--
-- Name: questions; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.questions (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(255) NOT NULL,
    marks numeric(38,2) NOT NULL,
    "position" integer,
    title character varying(255) NOT NULL,
    type smallint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    exam_id character varying(255) NOT NULL,
    question_options_id character varying(255),
    correct_option boolean,
    CONSTRAINT questions_type_check CHECK (((type >= 0) AND (type <= 4)))
);


ALTER TABLE public.questions OWNER TO arihant;

--
-- Name: ratings; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.ratings (
    id character varying(255) NOT NULL,
    comment character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    rating numeric(2,1) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    course_id bigint,
    user_id character varying(255)
);


ALTER TABLE public.ratings OWNER TO arihant;

--
-- Name: refresh_token; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.refresh_token (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    client character varying(255),
    ipaddress character varying(255)
);


ALTER TABLE public.refresh_token OWNER TO arihant;

--
-- Name: report_card; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.report_card (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    grade character varying(255) NOT NULL,
    obtained_marks numeric(38,2) NOT NULL,
    percentage numeric(38,2) NOT NULL,
    total_marks numeric(38,2) NOT NULL,
    exam_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


ALTER TABLE public.report_card OWNER TO arihant;

--
-- Name: review; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.review (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    vote_type smallint NOT NULL,
    course_id bigint,
    user_id character varying(255),
    CONSTRAINT review_vote_type_check CHECK (((vote_type >= 0) AND (vote_type <= 1)))
);


ALTER TABLE public.review OWNER TO arihant;

--
-- Name: tags; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.tags (
    id character varying(255) NOT NULL,
    name character varying(50) NOT NULL
);


ALTER TABLE public.tags OWNER TO arihant;

--
-- Name: users; Type: TABLE; Schema: public; Owner: arihant
--

CREATE TABLE public.users (
    id character varying(255) NOT NULL,
    email character varying(255),
    name character varying(255),
    password character varying(255),
    role character varying(255),
    username character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    is_active boolean,
    is_banned boolean,
    is_deleted boolean,
    is_verified boolean,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['STUDENT'::character varying, 'INSTRUCTOR'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO arihant;

--
-- Data for Name: ai_chat_bot; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.ai_chat_bot (id, completion_tokens, conversation_id, cost, course_id, created_at, feedback, flagged, helpful, lesson_id, max_tokens, message_order, model, page, prompt, prompt_tokens, response, role, session_id, temperature, total_tokens, user_id) FROM stdin;
3ceff4d6-5919-42c6-b24d-6af66dd112e6	74	\N	\N	\N	2025-12-18 16:23:46.056867+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"hi"}	36	Hello there! 👋\n\nI'm here to help you learn and answer any questions you might have. What's on your mind today? Are you working on a specific subject, trying to understand a new concept, or just curious about something?\n\nDon't hesitate to ask me anything! I'll do my best to explain it clearly and concisely. 😊	\N	\N	\N	74	1a8f3943-acc7-42da-a007-83066bd39c52
9adc84b3-6f4c-463f-9f0a-02a6564f44e3	77	\N	\N	\N	2025-12-18 16:26:53.674831+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"hi"}	36	Hello there! 👋\n\nI'm here to help you learn and answer any questions you might have. Think of me as your friendly teaching assistant.\n\nWhat's on your mind today? Are you working on a specific subject, trying to understand a new concept, or just curious about something? Don't hesitate to ask!\n\nI'm ready when you are. 😊	\N	\N	\N	77	1a8f3943-acc7-42da-a007-83066bd39c52
483cb9f2-d5b9-4818-b3a1-73cdde67301a	998	\N	\N	\N	2025-12-18 16:27:34.621192+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"generate some data but not more that 500 words"}	47	Here's some data, presented in a few different formats, keeping it under 500 words:\n\n## Sample Data Set: Customer Purchase History\n\nThis dataset simulates a small collection of customer purchase records.\n\n**Format 1: Tabular Data (Simplified)**\n\n| Customer ID | Product ID | Purchase Date | Quantity | Price |\n|---|---|---|---|---|\n| 101 | A123 | 2023-10-26 | 2 | 15.99 |\n| 102 | B456 | 2023-10-26 | 1 | 49.50 |\n| 101 | C789 | 2023-10-27 | 3 | 5.00 |\n| 103 | A123 | 2023-10-27 | 1 | 15.99 |\n| 102 | D012 | 2023-10-28 | 1 | 25.00 |\n| 101 | B456 | 2023-10-28 | 1 | 49.50 |\n| 104 | E345 | 2023-10-29 | 2 | 10.00 |\n| 103 | A123 | 2023-10-29 | 1 | 15.99 |\n| 101 | D012 | 2023-10-30 | 1 | 25.00 |\n| 105 | C789 | 2023-10-30 | 5 | 5.00 |\n\n**Format 2: JSON (JavaScript Object Notation)**\n\n```json\n[\n  {\n    "customer_id": 101,\n    "product_id": "A123",\n    "purchase_date": "2023-10-26",\n    "quantity": 2,\n    "price": 15.99\n  },\n  {\n    "customer_id": 102,\n    "product_id": "B456",\n    "purchase_date": "2023-10-26",\n    "quantity": 1,\n    "price": 49.50\n  },\n  {\n    "customer_id": 101,\n    "product_id": "C789",\n    "purchase_date": "2023-10-27",\n    "quantity": 3,\n    "price": 5.00\n  },\n  {\n    "customer_id": 103,\n    "product_id": "A123",\n    "purchase_date": "2023-10-27",\n    "quantity": 1,\n    "price": 15.99\n  },\n  {\n    "customer_id": 102,\n    "product_id": "D012",\n    "purchase_date": "2023-10-28",\n    "quantity": 1,\n    "price": 25.00\n  },\n  {\n    "customer_id": 101,\n    "product_id": "B456",\n    "purchase_date": "2023-10-28",\n    "quantity": 1,\n    "price": 49.50\n  },\n  {\n    "customer_id": 104,\n    "product_id": "E345",\n    "purchase_date": "2023-10-29",\n    "quantity": 2,\n    "price": 10.00\n  },\n  {\n    "customer_id": 103,\n    "product_id": "A123",\n    "purchase_date": "2023-10-29",\n    "quantity": 1,\n    "price": 15.99\n  },\n  {\n    "customer_id": 101,\n    	\N	\N	\N	998	1a8f3943-acc7-42da-a007-83066bd39c52
54857016-20ea-43e5-8d72-655235cd72ee	28	\N	\N	\N	2025-12-18 16:30:43.852822+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"hi"}	36	Hi there! I'm happy to help you learn and answer any questions you might have. What's on your mind today? 😊	\N	\N	\N	28	1a8f3943-acc7-42da-a007-83066bd39c52
50449fba-324a-4c0d-8a61-a90e98f5889e	46	\N	\N	\N	2025-12-18 16:33:58.741525+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"hi"}	36	Hello! It's great to hear from you. How can I help you learn something new today? Do you have any specific questions or topics you're curious about? I'm here to assist you with your studies!	\N	\N	\N	46	1a8f3943-acc7-42da-a007-83066bd39c52
1a08c213-c4f1-4a2a-999d-9446f92b4037	169	\N	\N	\N	2025-12-22 17:38:13.25227+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"h"}	36	It looks like you've sent a single letter, "h".  That's a great start!\n\nTo help me understand what you're looking for, could you please tell me more about what you'd like to learn or what question you have? For example, are you:\n\n*   **Looking for information about the letter "h" itself?** We could talk about its pronunciation, its role in words, or even its history.\n*   **Trying to start a sentence or a question?** If so, what were you hoping to say or ask?\n*   **Testing out the system?** That's perfectly fine too!\n\nThe more context you can give me, the better I can assist you. I'm here to help you learn, so don't hesitate to elaborate!	\N	\N	\N	169	1a8f3943-acc7-42da-a007-83066bd39c52
8ffd587e-643a-410f-aeef-b0e8a357a95c	86	\N	\N	\N	2025-12-22 17:38:59.109158+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"Hii I just create this ai chat bot using gemini"}	47	That's fantastic! Congratulations on creating your AI chatbot using Gemini. That's a really exciting project.\n\nWhat are you most proud of with your chatbot so far? Is there anything specific you're hoping to learn or achieve with it next? I'm here to help if you have any questions about AI, Gemini, or anything else related to your project.\n\nLet me know what's on your mind!	\N	\N	\N	86	1a8f3943-acc7-42da-a007-83066bd39c52
3e54f370-4eb2-4f4d-8e8d-cbb1fe5dba4c	1000	\N	\N	\N	2025-12-22 17:40:28.586839+05:30	\N	\N	\N	\N	\N	\N	gemini-2.5-flash-lite	\N	{"message":"ERROR:  character with byte sequence 0xf0 0x9f 0x91 0x8b in encoding \\"UTF8\\" has no equivalent in encoding \\"WIN1252\\" can you give me the solution?? PSQL"}	88	This error message:\n\n```\nERROR: character with byte sequence 0xf0 0x9f 0x91 0x8b in encoding "UTF8" has no equivalent in encoding "WIN1252"\n```\n\nis a common issue when working with PostgreSQL (PSQL) and character encodings. Let's break down what it means and how to solve it.\n\n**What the Error Means**\n\n*   **UTF8:** This is a very common and versatile character encoding that can represent almost any character from any language, including emojis, special symbols, and characters from various alphabets. The byte sequence `0xf0 0x9f 0x91 0x8b` is the UTF-8 representation of a specific character. In this case, it's the "OK hand" emoji (👌).\n*   **WIN1252 (also known as Windows-1252):** This is an older, more limited character encoding primarily used on Windows systems. It has a much smaller set of characters than UTF-8 and doesn't include many of the modern characters, especially emojis.\n*   **"has no equivalent":** This means that the character represented by `0xf0 0x9f 0x91 0x8b` (the 👌 emoji) exists in UTF-8, but there is no corresponding character in the WIN1252 encoding.\n\n**The Problem:**\n\nThe error occurs when you are trying to insert or process data that contains a character encoded in UTF-8 into a system or database that is expecting or configured to use WIN1252. PostgreSQL itself is likely configured to use UTF-8 (which is the recommended default), but the client application or the way you're connecting to the database might be set to WIN1252, or you might be trying to export data in a format that expects WIN1252.\n\n**Solutions**\n\nHere are the most common and effective solutions:\n\n1.  **Ensure Your Client and Connection Use UTF-8 (Recommended):**\n    This is the **best and most robust solution**. You want your entire data pipeline to handle UTF-8 correctly.\n\n    *   **Client Application:** If you're using a GUI tool (like pgAdmin, DBeaver, etc.) or a command-line client, make sure its encoding settings are set to UTF-8.\n    *   **Connection String/Environment Variables:** When connecting to PostgreSQL from an application (e.g., Python, Java, Node.js), ensure your connection string or environment variables specify UTF-8. For example, in many applications, you might see something like `PGCLIENTENCODING=UTF8` or a similar setting in your database driver configuration.\n    *   **PostgreSQL Server Configuration:** While less likely to be the direct cause of this specific error if you're inserting into a UTF-8 database, it's good practice to ensure your PostgreSQL server's `client_encoding` parameter is set to UTF-8. You can check this with `SHOW client_encoding;` in psql.\n\n2.  **Convert or Sanitize the Data Before Inserting:**\n    If you absolutely *cannot* change the client encoding to UTF-8 (which is rare and usually indicates a legacy system limitation), you'll need to clean the data before it reaches the database.\n\n    *   **Remove Unsupported Characters:** You can write code to detect and remove characters that are not representable in WIN1252. This is complex because you need to know which characters are problematic.\n    *   **Replace Unsupported Characters:** Instead of removing, you could replace them with a placeholder like `?` or an empty string.\n\n    **Example (Conceptual Python):**\n\n    ```python\n    import re\n\n    def sanitize_for_win1252(text):\n        # This is a simplified example. A truly robust solution is complex.\n        # It's better to use a library that understands character sets.\n        # For emojis and many other symbols, they are outside WIN1252.\n        # A common approach is to remove them if WIN1252 is a strict requirement.\n        # This regex attempts to remove characters outside the basic ASCII range,\n        # but it's not perfect for all WIN1252 characters.\n        # A more accurate approach would be to define allowed WIN1252 ranges.\n\n        # A simpler, more direct approach for this specific error (emojis):\n        # Remove characters that are likely emojis or outside common ranges.\n        # This regex targets characters outside the basic multilingual plane (BMP)\n        # and some common symbols	\N	\N	\N	1000	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: ai_daily_usage; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.ai_daily_usage (id, cost_used, daily_cost_limit, daily_question_limit, daily_token_limit, last_updated, questions_used, tokens_used, usage_date, user_id) FROM stdin;
ad36b72c-37bf-4a28-93ce-07f631c84e8e	\N	\N	\N	\N	\N	0	0	2025-12-18	1a8f3943-acc7-42da-a007-83066bd39c52 
b0651cfc-8b80-420d-b611-b941c8afebb6	\N	\N	\N	\N	\N	7	1414	2025-12-18	1a8f3943-acc7-42da-a007-83066bd39c52
69f711a1-5aed-4fa0-961b-4139deb98a0d	\N	\N	\N	\N	\N	3	1426	2025-12-22	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: asset; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.asset (id, created_at, name, public_id, type, updated_at, url, user_id) FROM stdin;
ba373358-f0ad-486d-be05-f020e6cf4e3a	2025-09-18 13:30:58.586249	etst.jpg	lms/fg2qfhqdhbatsnqjgzz8	image	2025-09-18 13:30:58.586249	https://res.cloudinary.com/dbbyts6io/image/upload/v1758182456/lms/fg2qfhqdhbatsnqjgzz8.jpg	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: asset_meta; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.asset_meta (id, created_at, description, extension, mime_type, size, updated_at, visibility, asset_id) FROM stdin;
06d55e95-d8ac-4369-a91c-15393e06b2a3	2025-09-18 13:30:58.601874		jpg	image/jpeg	3075007	2025-09-18 13:30:58.601874	null	ba373358-f0ad-486d-be05-f020e6cf4e3a
\.


--
-- Data for Name: blog; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.blog (id, tag, category, content, created_at, description, image_url, read_time, slug, status, title, updated_at, user_id, is_featured) FROM stdin;
2f8db48b-0c53-47ed-ba6d-c3f83fec5edc	EDUCATION	4	Content is required	2025-09-20 15:14:07.247313	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd	0	Title is requiredasd	2025-09-20 15:14:07.247313	1a8f3943-acc7-42da-a007-83066bd39c52	f
1e159282-8a2a-4a45-b712-51a5050642c2	EDUCATION	2	Content is required	2025-09-20 15:15:05.31595	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-a1f019	0	Title is requiredasd ef93	2025-09-20 15:15:05.31595	1a8f3943-acc7-42da-a007-83066bd39c52	f
a1d9cbd7-c32f-4ee8-a038-64e1384d83b8	EDUCATION	2	Content is required	2025-09-20 15:15:08.070972	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-79b179	0	Title is requiredasd 268d	2025-09-20 15:15:08.070972	1a8f3943-acc7-42da-a007-83066bd39c52	f
91817d81-d77e-4c79-bdef-4a6926bd342f	EDUCATION	0	Content is required	2025-09-20 15:15:16.4006	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-b10997	0	Title is requiredasd a5bb	2025-09-20 15:15:16.4006	1a8f3943-acc7-42da-a007-83066bd39c52	f
e0c681b0-a8a5-446d-ae09-740b1340bb3e	EDUCATION	0	Content is required	2025-09-20 15:15:17.722142	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-4a3179	0	Title is requiredasd 0408	2025-09-20 15:15:17.722142	1a8f3943-acc7-42da-a007-83066bd39c52	f
b403db06-dba0-4fdf-b52d-af0006bf1a09	EDUCATION	0	Content is required	2025-09-20 15:15:41.98879	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-013002	0	Title is requiredasd e666	2025-09-20 15:15:41.98879	1a8f3943-acc7-42da-a007-83066bd39c52	f
14b604d2-4563-4a13-892b-369241fb1fb3	EDUCATION	0	Content is required	2025-09-20 15:15:42.864088	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-097844	0	Title is requiredasd a8d8	2025-09-20 15:15:42.864088	1a8f3943-acc7-42da-a007-83066bd39c52	f
89b26d25-2ca1-4fb5-a1ca-7a44580bbdf5	EDUCATION	0	Content is required	2025-09-20 15:15:43.626295	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-1cb8d1	0	Title is requiredasd bb46	2025-09-20 15:15:43.626295	1a8f3943-acc7-42da-a007-83066bd39c52	f
27290fef-abde-4061-bf3a-2c94a4720489	EDUCATION	0	Content is required	2025-09-20 15:15:44.473088	Description is required	https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA	Read Time is required	Slug-is-requiredasd-0f9c9e	0	Title is requiredasd 3f42	2025-09-20 15:15:44.473088	1a8f3943-acc7-42da-a007-83066bd39c52	f
00a1d1cd-2a06-4f62-90fa-faf99937eec0	EDUCATION	4	Content is required	2025-09-26 17:29:24.682263	Description is required	ImageUrl is required	Read Time is required	Slug-is-requiredasd-1	0	Title is requiredasd555	2025-09-26 17:29:24.682263	1a8f3943-acc7-42da-a007-83066bd39c52	f
497eff24-89bc-478f-a262-2d03ec6d160a	EDUCATION	2	Content is required	2025-09-20 15:15:07.328046	Description is required	ImageUrl is required	Read Time is required	Slug is requiredasd-update-arihant	0	Title is requiredasd-update-arihant	2025-09-26 18:22:20.222788	ac31c162-5650-4937-a9b6-d73063aae295	f
\.


--
-- Data for Name: blog_comment; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.blog_comment (id, comment, created_at, updated_at, blog_id, parent_id, user_id) FROM stdin;
474185	test comment	2025-09-25 11:52:25.539465	2025-09-25 11:52:25.539465	91817d81-d77e-4c79-bdef-4a6926bd342f	\N	1a8f3943-acc7-42da-a007-83066bd39c52
474189	test comment	2025-09-25 13:31:44.34593	2025-09-25 13:31:44.34593	91817d81-d77e-4c79-bdef-4a6926bd342f	474185	1a8f3943-acc7-42da-a007-83066bd39c52
474179	updated comment	2025-09-25 13:32:18.290115	2025-09-25 13:32:18.290115	91817d81-d77e-4c79-bdef-4a6926bd342f	474185	1a8f3943-acc7-42da-a007-83066bd39c52
474139	test comment	2025-09-25 11:52:25.539465	2025-09-25 11:52:25.539465	91817d81-d77e-4c79-bdef-4a6926bd342f	\N	1a8f3943-acc7-42da-a007-83066bd39c52
4741797	test comment	2025-09-25 11:52:25.539465	2025-09-25 11:52:25.539465	91817d81-d77e-4c79-bdef-4a6926bd342f	474185	1a8f3943-acc7-42da-a007-83066bd39c52
d97d9199-7a76-4096-a83d-5961fc465445	PostMan comment	2025-09-25 16:18:23.224068	2025-09-25 16:18:23.224068	27290fef-abde-4061-bf3a-2c94a4720489	\N	1a8f3943-acc7-42da-a007-83066bd39c52
42760e57-74b3-47d6-8fc0-f07fcee8a88a	PostMan comment	2025-09-25 17:11:23.290879	2025-09-25 17:11:23.290879	27290fef-abde-4061-bf3a-2c94a4720489	\N	1a8f3943-acc7-42da-a007-83066bd39c52
4b63bd90-0fa9-409c-95bf-004d2fda5731	PostMan comment	2025-09-25 17:11:27.723773	2025-09-25 17:11:27.723773	27290fef-abde-4061-bf3a-2c94a4720489	\N	1a8f3943-acc7-42da-a007-83066bd39c52
eeb89dbf-9f54-4ced-9f16-fb90d7665c66	PostMan comment	2025-09-25 17:11:28.908407	2025-09-25 17:11:28.908407	27290fef-abde-4061-bf3a-2c94a4720489	\N	1a8f3943-acc7-42da-a007-83066bd39c52
cbc64082-ddc4-4ac6-a5ab-4851d400395a	PostMan comment	2025-09-25 17:13:04.70148	2025-09-25 17:13:04.70148	27290fef-abde-4061-bf3a-2c94a4720489	\N	1a8f3943-acc7-42da-a007-83066bd39c52
1d87f352-03d9-4345-b929-757c25079664	PostMan comment	2025-09-25 17:13:05.920401	2025-09-25 17:13:05.920401	27290fef-abde-4061-bf3a-2c94a4720489	\N	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.category (id, description, name, slug, icon, is_featured) FROM stdin;
9431a581-f19f-4ae4-a71f-f8afa449ee75	Test Category	Phone Android	phone-android-3465	Code	\N
a1c2e3d4-5678-4f90-9abc-def123456789	Create mobile apps for Android and iOS	Mobile Development	mobile-dev	Smartphone	f
0c87dcfe-7d5c-4367-a043-f542dad51442	Test Category	Phone Android	phone-android-8278	Code	\N
c3e4f5a6-7890-4b12-9cde-f34567890abc	AI, ML, and deep learning concepts	Machine Learning	ml	Brain	f
d4f5a6b7-8901-4c23-abcd-4567890abcde	Learn AWS, Azure, and cloud infrastructure	Cloud Computing	cloud	Cloud	f
e5a6b7c8-9012-4d34-bcde-567890abcdef	Secure applications, networks, and data	Cybersecurity	cybersecurity	Shield	f
f6b7c8d9-0123-4e45-cdef-67890abcdef1	Learn SQL, NoSQL, and database design	Databases	databases	Database	f
07c8d9e0-1234-4f56-def0-7890abcdef12	Design user-friendly applications	UI/UX Design	ui-ux	Palette	f
18d9e0f1-2345-4056-ef01-890abcdef123	Learn business, management, and leadership skills	Business	business	Briefcase	f
29e0f102-3456-4167-f012-90abcdef1234	Master finance, accounting, and investment	Finance	finance	DollarSign	f
3af10213-4567-4278-0123-0abcdef12345	Digital marketing and SEO strategies	Marketing	marketing	TrendingUp	f
4bf21324-5678-4389-1234-abcdef123456	Learn foreign and programming languages	Languages	languages	Globe	f
5c031435-6789-449a-2345-bcdef1234567	Improve soft skills and personal growth	Personal Development	personal-dev	BookOpen	f
6d142546-7890-45ab-3456-cdef12345678	Nutrition, workouts, and wellness	Health & Fitness	health-fitness	Heart	f
7e253657-8901-46bc-4567-def123456789	Learn instruments, vocals, and music theory	Music	music	Music	f
8f364768-9012-47cd-5678-ef1234567890	Capture moments and edit like a pro	Photography	photography	Camera	f
90475879-0123-48de-6789-f1234567890a	Teaching and educational skills	Education	education	GraduationCap	f
182538a9-13b6-4053-8d9b-693a51a4a768	Test Category	Phone Android	phone-android-1468	Code	\N
f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	Learn how to build websites and applications	Web Development	web-dev	Code	t
b2d3e4f5-6789-4a01-8bcd-ef234567890a	Analyze data and build predictive models	Data Science	data-science	BarChart3	t
a41da324-8ff7-44d1-aaff-9bb5eac4ac8b	Test Category	Phone Android	phone-android	Code	\N
\.


--
-- Data for Name: contact_us; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.contact_us (id, department, email, message, name, phone, subject) FROM stdin;
08bb745e-394b-489a-8f03-96c087607524	1	arihant@gmail.com	Hii hii hii hii hjijzoicvhoasdbnvcoiusadbhvuiosdbvi usd	Arihant	+919672670732	Hii
1cf1ace6-e411-4230-86c3-2bb6f5e69485	0	Email is required	Message is required	Name is required	Phone is required	Subject is required
\.


--
-- Data for Name: course_views; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.course_views (id, created_at, user_id, course_id, ip, referrer, user_agent) FROM stdin;
552	2025-09-24 15:06:30.182	user:1a8f3943-acc7-42da-a007-83066bd39c52	9	\N	\N	\N
553	2025-09-24 15:06:34.807	guest:guest:099c19e2-698b-4e3a-b9b0-174bb9aa72a9	9	\N	\N	\N
6	2025-09-25 10:32:27.444777	user:1a8f3943-acc7-42da-a007-83066bd39c52-6223fc	9	\N	\N	\N
7	2025-09-25 10:32:41.028415	user:1a8f3943-acc7-42da-a007-83066bd39c52-672b27	9	\N	\N	\N
8	2025-09-25 10:32:49.778753	user:1a8f3943-acc7-42da-a007-83066bd39c52-7fbf47	9	\N	\N	\N
88	2025-09-25 10:41:55.237022	user:1a8f3943-acc7-42da-a007-83066bd39c52-aec15a	53	\N	\N	\N
\.


--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.courses (id, created_at, description, slug, title, updated_at, category_id, instructor_id, user_id, is_featured, pricing_plan_id) FROM stdin;
302	2025-09-23 15:27:48.722	test test description 1	arihant	test title 1	2025-09-23 15:27:48.722	a41da324-8ff7-44d1-aaff-9bb5eac4ac8b	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	\N
53	2025-09-23 13:30:00	test test description-8	test-title-slug	test title updated	2025-09-23 13:30:00	a41da324-8ff7-44d1-aaff-9bb5eac4ac8b	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
202	2025-09-23 13:30:00	test test description 1	test-title-slug-1	test title 1	2025-09-23 13:30:00	a41da324-8ff7-44d1-aaff-9bb5eac4ac8b	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	\N
11	2025-09-23 13:30:00	Learn Android in Mobile Dev	mobile-dev-1	Android	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
84	2025-09-23 13:30:00	Learn Operations in Business	business-9	Operations	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
85	2025-09-23 13:30:00	Learn Scaling Business in Business	business-10	Scaling Business	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
86	2025-09-23 13:30:00	Learn Personal Finance in Finance	finance-1	Personal Finance	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
252	2025-09-23 13:30:00	test test description 1	test-title-slug-1-5	test title 1	2025-09-23 13:30:00	a41da324-8ff7-44d1-aaff-9bb5eac4ac8b	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	\N
1	2025-09-23 13:30:00	Learn HTML & CSS in Web Dev	web-dev-1	HTML & CSS	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	f	8748-884-DAG
9	2025-09-23 13:30:00	Learn GraphQL in Web Dev	web-dev-9	GraphQL	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	t	8748-884-DAG
2	2025-09-23 13:30:00	Learn JavaScript in Web Dev	web-dev-2	JavaScript	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
3	2025-09-23 13:30:00	Learn React in Web Dev	web-dev-3	React	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
4	2025-09-23 13:30:00	Learn Next.js in Web Dev	web-dev-4	Next.js	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
5	2025-09-23 13:30:00	Learn Tailwind in Web Dev	web-dev-5	Tailwind	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
6	2025-09-23 13:30:00	Learn Node.js in Web Dev	web-dev-6	Node.js	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
7	2025-09-23 13:30:00	Learn TypeScript in Web Dev	web-dev-7	TypeScript	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
127	2025-09-23 13:30:00	Learn Guitar in Music	music-1	Guitar	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
8	2025-09-23 13:30:00	Learn REST APIs in Web Dev	web-dev-8	REST APIs	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
49	2025-09-23 13:30:00	Learn Terraform in Cloud	cloud-9	Terraform	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
97	2025-09-23 13:30:00	Learn SEO in Marketing	marketing-2	SEO	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
73	2025-09-23 13:30:00	Learn Accessibility in Ui Ux	ui-ux-8	Accessibility	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
74	2025-09-23 13:30:00	Learn Mobile Design in Ui Ux	ui-ux-9	Mobile Design	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
75	2025-09-23 13:30:00	Learn UI Animation in Ui Ux	ui-ux-10	UI Animation	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
76	2025-09-23 13:30:00	Learn Business Strategy in Business	business-1	Business Strategy	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
77	2025-09-23 13:30:00	Learn Entrepreneurship in Business	business-2	Entrepreneurship	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
78	2025-09-23 13:30:00	Learn Management in Business	business-3	Management	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
79	2025-09-23 13:30:00	Learn Leadership in Business	business-4	Leadership	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
80	2025-09-23 13:30:00	Learn Project Management in Business	business-5	Project Management	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
81	2025-09-23 13:30:00	Learn Agile in Business	business-6	Agile	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
82	2025-09-23 13:30:00	Learn Lean Startup in Business	business-7	Lean Startup	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
83	2025-09-23 13:30:00	Learn HR Basics in Business	business-8	HR Basics	2025-09-23 13:30:00	18d9e0f1-2345-4056-ef01-890abcdef123	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
87	2025-09-23 13:30:00	Learn Accounting in Finance	finance-2	Accounting	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
88	2025-09-23 13:30:00	Learn Investing in Finance	finance-3	Investing	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
89	2025-09-23 13:30:00	Learn Financial Modeling in Finance	finance-4	Financial Modeling	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
90	2025-09-23 13:30:00	Learn Corporate Finance in Finance	finance-5	Corporate Finance	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
91	2025-09-23 13:30:00	Learn Banking in Finance	finance-6	Banking	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
92	2025-09-23 13:30:00	Learn Taxation in Finance	finance-7	Taxation	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
93	2025-09-23 13:30:00	Learn FinTech in Finance	finance-8	FinTech	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
94	2025-09-23 13:30:00	Learn Stock Market in Finance	finance-9	Stock Market	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
95	2025-09-23 13:30:00	Learn Risk Management in Finance	finance-10	Risk Management	2025-09-23 13:30:00	29e0f102-3456-4167-f012-90abcdef1234	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
96	2025-09-23 13:30:00	Learn Digital Marketing in Marketing	marketing-1	Digital Marketing	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
98	2025-09-23 13:30:00	Learn Content Marketing in Marketing	marketing-3	Content Marketing	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
99	2025-09-23 13:30:00	Learn Social Media in Marketing	marketing-4	Social Media	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
100	2025-09-23 13:30:00	Learn Email Marketing in Marketing	marketing-5	Email Marketing	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
101	2025-09-23 13:30:00	Learn PPC Ads in Marketing	marketing-6	PPC Ads	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
102	2025-09-23 13:30:00	Learn Influencer Marketing in Marketing	marketing-7	Influencer Marketing	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
103	2025-09-23 13:30:00	Learn Analytics in Marketing	marketing-8	Analytics	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
104	2025-09-23 13:30:00	Learn Branding in Marketing	marketing-9	Branding	2025-09-23 13:30:00	3af10213-4567-4278-0123-0abcdef12345	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
105	2025-09-23 13:30:00	Learn English in Languages	languages-1	English	2025-09-23 13:30:00	4bf21324-5678-4389-1234-abcdef123456	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
106	2025-09-23 13:30:00	Learn Spanish in Languages	languages-2	Spanish	2025-09-23 13:30:00	4bf21324-5678-4389-1234-abcdef123456	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
107	2025-09-23 13:30:00	Learn French in Languages	languages-3	French	2025-09-23 13:30:00	4bf21324-5678-4389-1234-abcdef123456	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
108	2025-09-23 13:30:00	Learn German in Languages	languages-4	German	2025-09-23 13:30:00	4bf21324-5678-4389-1234-abcdef123456	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
109	2025-09-23 13:30:00	Learn Japanese in Languages	languages-5	Japanese	2025-09-23 13:30:00	4bf21324-5678-4389-1234-abcdef123456	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
110	2025-09-23 13:30:00	Learn Mandarin in Languages	languages-6	Mandarin	2025-09-23 13:30:00	4bf21324-5678-4389-1234-abcdef123456	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
111	2025-09-23 13:30:00	Learn Time Management in Personal Dev	personal-dev-1	Time Management	2025-09-23 13:30:00	5c031435-6789-449a-2345-bcdef1234567	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
112	2025-09-23 13:30:00	Learn Public Speaking in Personal Dev	personal-dev-2	Public Speaking	2025-09-23 13:30:00	5c031435-6789-449a-2345-bcdef1234567	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
113	2025-09-23 13:30:00	Learn Critical Thinking in Personal Dev	personal-dev-3	Critical Thinking	2025-09-23 13:30:00	5c031435-6789-449a-2345-bcdef1234567	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
114	2025-09-23 13:30:00	Learn Creativity in Personal Dev	personal-dev-4	Creativity	2025-09-23 13:30:00	5c031435-6789-449a-2345-bcdef1234567	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
115	2025-09-23 13:30:00	Learn Leadership in Personal Dev	personal-dev-5	Leadership	2025-09-23 13:30:00	5c031435-6789-449a-2345-bcdef1234567	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
116	2025-09-23 13:30:00	Learn Emotional Intelligence in Personal Dev	personal-dev-6	Emotional Intelligence	2025-09-23 13:30:00	5c031435-6789-449a-2345-bcdef1234567	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
117	2025-09-23 13:30:00	Learn Nutrition in Health Fitness	health-fitness-1	Nutrition	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
118	2025-09-23 13:30:00	Learn Yoga in Health Fitness	health-fitness-2	Yoga	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
119	2025-09-23 13:30:00	Learn Strength Training in Health Fitness	health-fitness-3	Strength Training	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
10	2025-09-23 13:30:00	Learn Fullstack in Web Dev	web-dev-10	Fullstack	2025-09-23 13:30:00	f9b27c1c-1d88-4c7f-8a5d-9cfae9e01a11	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
12	2025-09-23 13:30:00	Learn iOS in Mobile Dev	mobile-dev-2	iOS	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
13	2025-09-23 13:30:00	Learn Flutter in Mobile Dev	mobile-dev-3	Flutter	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
14	2025-09-23 13:30:00	Learn React Native in Mobile Dev	mobile-dev-4	React Native	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
15	2025-09-23 13:30:00	Learn Firebase in Mobile Dev	mobile-dev-5	Firebase	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
16	2025-09-23 13:30:00	Learn Kotlin in Mobile Dev	mobile-dev-6	Kotlin	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
17	2025-09-23 13:30:00	Learn SwiftUI in Mobile Dev	mobile-dev-7	SwiftUI	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
18	2025-09-23 13:30:00	Learn App Testing in Mobile Dev	mobile-dev-8	App Testing	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
19	2025-09-23 13:30:00	Learn Mobile Security in Mobile Dev	mobile-dev-9	Mobile Security	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
20	2025-09-23 13:30:00	Learn Cross Platform in Mobile Dev	mobile-dev-10	Cross Platform	2025-09-23 13:30:00	a1c2e3d4-5678-4f90-9abc-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
21	2025-09-23 13:30:00	Learn Python in Data Science	data-science-1	Python	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
22	2025-09-23 13:30:00	Learn NumPy in Data Science	data-science-2	NumPy	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
23	2025-09-23 13:30:00	Learn Pandas in Data Science	data-science-3	Pandas	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
24	2025-09-23 13:30:00	Learn Matplotlib in Data Science	data-science-4	Matplotlib	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
25	2025-09-23 13:30:00	Learn Statistics in Data Science	data-science-5	Statistics	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
26	2025-09-23 13:30:00	Learn SQL in Data Science	data-science-6	SQL	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
27	2025-09-23 13:30:00	Learn Data Wrangling in Data Science	data-science-7	Data Wrangling	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
28	2025-09-23 13:30:00	Learn Data Cleaning in Data Science	data-science-8	Data Cleaning	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
29	2025-09-23 13:30:00	Learn Visualization in Data Science	data-science-9	Visualization	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
30	2025-09-23 13:30:00	Learn End-to-End Project in Data Science	data-science-10	End-to-End Project	2025-09-23 13:30:00	b2d3e4f5-6789-4a01-8bcd-ef234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
31	2025-09-23 13:30:00	Learn Intro to ML in Ml	ml-1	Intro to ML	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
32	2025-09-23 13:30:00	Learn Linear Regression in Ml	ml-2	Linear Regression	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
33	2025-09-23 13:30:00	Learn Classification in Ml	ml-3	Classification	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
34	2025-09-23 13:30:00	Learn Clustering in Ml	ml-4	Clustering	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
35	2025-09-23 13:30:00	Learn Deep Learning in Ml	ml-5	Deep Learning	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
36	2025-09-23 13:30:00	Learn TensorFlow in Ml	ml-6	TensorFlow	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
37	2025-09-23 13:30:00	Learn PyTorch in Ml	ml-7	PyTorch	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
38	2025-09-23 13:30:00	Learn NLP in Ml	ml-8	NLP	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
39	2025-09-23 13:30:00	Learn Computer Vision in Ml	ml-9	Computer Vision	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
40	2025-09-23 13:30:00	Learn Reinforcement Learning in Ml	ml-10	Reinforcement Learning	2025-09-23 13:30:00	c3e4f5a6-7890-4b12-9cde-f34567890abc	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
41	2025-09-23 13:30:00	Learn AWS Basics in Cloud	cloud-1	AWS Basics	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
42	2025-09-23 13:30:00	Learn Azure in Cloud	cloud-2	Azure	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
43	2025-09-23 13:30:00	Learn Google Cloud in Cloud	cloud-3	Google Cloud	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
44	2025-09-23 13:30:00	Learn Docker in Cloud	cloud-4	Docker	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
45	2025-09-23 13:30:00	Learn Kubernetes in Cloud	cloud-5	Kubernetes	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
46	2025-09-23 13:30:00	Learn CI/CD in Cloud	cloud-6	CI/CD	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
47	2025-09-23 13:30:00	Learn DevOps in Cloud	cloud-7	DevOps	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
48	2025-09-23 13:30:00	Learn Cloud Security in Cloud	cloud-8	Cloud Security	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
120	2025-09-23 13:30:00	Learn Cardio in Health Fitness	health-fitness-4	Cardio	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
121	2025-09-23 13:30:00	Learn Weight Loss in Health Fitness	health-fitness-5	Weight Loss	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
122	2025-09-23 13:30:00	Learn Mental Health in Health Fitness	health-fitness-6	Mental Health	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
123	2025-09-23 13:30:00	Learn Sports Training in Health Fitness	health-fitness-7	Sports Training	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
124	2025-09-23 13:30:00	Learn Meditation in Health Fitness	health-fitness-8	Meditation	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
125	2025-09-23 13:30:00	Learn Wellness in Health Fitness	health-fitness-9	Wellness	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
126	2025-09-23 13:30:00	Learn Healthy Habits in Health Fitness	health-fitness-10	Healthy Habits	2025-09-23 13:30:00	6d142546-7890-45ab-3456-cdef12345678	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
128	2025-09-23 13:30:00	Learn Piano in Music	music-2	Piano	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
129	2025-09-23 13:30:00	Learn Drums in Music	music-3	Drums	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
130	2025-09-23 13:30:00	Learn Singing in Music	music-4	Singing	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
131	2025-09-23 13:30:00	Learn Music Theory in Music	music-5	Music Theory	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
132	2025-09-23 13:30:00	Learn Songwriting in Music	music-6	Songwriting	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
133	2025-09-23 13:30:00	Learn DJing in Music	music-7	DJing	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
134	2025-09-23 13:30:00	Learn Violin in Music	music-8	Violin	2025-09-23 13:30:00	7e253657-8901-46bc-4567-def123456789	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
135	2025-09-23 13:30:00	Learn Basics in Photography	photography-1	Basics	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
136	2025-09-23 13:30:00	Learn Portrait in Photography	photography-2	Portrait	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
137	2025-09-23 13:30:00	Learn Landscape in Photography	photography-3	Landscape	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
138	2025-09-23 13:30:00	Learn Editing in Photography	photography-4	Editing	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
139	2025-09-23 13:30:00	Learn Lighting in Photography	photography-5	Lighting	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
140	2025-09-23 13:30:00	Learn Street Photography in Photography	photography-6	Street Photography	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
141	2025-09-23 13:30:00	Learn Wildlife in Photography	photography-7	Wildlife	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
142	2025-09-23 13:30:00	Learn Drone Photography in Photography	photography-8	Drone Photography	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
143	2025-09-23 13:30:00	Learn Videography in Photography	photography-9	Videography	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
144	2025-09-23 13:30:00	Learn DSLR Mastery in Photography	photography-10	DSLR Mastery	2025-09-23 13:30:00	8f364768-9012-47cd-5678-ef1234567890	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
145	2025-09-23 13:30:00	Learn Teaching Basics in Education	education-1	Teaching Basics	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
146	2025-09-23 13:30:00	Learn Classroom Management in Education	education-2	Classroom Management	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
147	2025-09-23 13:30:00	Learn Online Teaching in Education	education-3	Online Teaching	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
148	2025-09-23 13:30:00	Learn Curriculum Design in Education	education-4	Curriculum Design	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
149	2025-09-23 13:30:00	Learn Student Engagement in Education	education-5	Student Engagement	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
150	2025-09-23 13:30:00	Learn Assessments in Education	education-6	Assessments	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
151	2025-09-23 13:30:00	Learn Special Education in Education	education-7	Special Education	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
152	2025-09-23 13:30:00	Learn EdTech in Education	education-8	EdTech	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
153	2025-09-23 13:30:00	Learn Learning Theories in Education	education-9	Learning Theories	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
154	2025-09-23 13:30:00	Learn Higher Education in Education	education-10	Higher Education	2025-09-23 13:30:00	90475879-0123-48de-6789-f1234567890a	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
50	2025-09-23 13:30:00	Learn Serverless in Cloud	cloud-10	Serverless	2025-09-23 13:30:00	d4f5a6b7-8901-4c23-abcd-4567890abcde	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
51	2025-09-23 13:30:00	Learn Intro to Security in Cybersecurity	cybersecurity-1	Intro to Security	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
52	2025-09-23 13:30:00	Learn Network Security in Cybersecurity	cybersecurity-2	Network Security	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
54	2025-09-23 13:30:00	Learn Ethical Hacking in Cybersecurity	cybersecurity-4	Ethical Hacking	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
55	2025-09-23 13:30:00	Learn Penetration Testing in Cybersecurity	cybersecurity-5	Penetration Testing	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
56	2025-09-23 13:30:00	Learn Malware Analysis in Cybersecurity	cybersecurity-6	Malware Analysis	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
57	2025-09-23 13:30:00	Learn Cryptography in Cybersecurity	cybersecurity-7	Cryptography	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
58	2025-09-23 13:30:00	Learn SOC Analyst in Cybersecurity	cybersecurity-8	SOC Analyst	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
59	2025-09-23 13:30:00	Learn Web Security in Cybersecurity	cybersecurity-9	Web Security	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
60	2025-09-23 13:30:00	Learn Cloud Security in Cybersecurity	cybersecurity-10	Cloud Security	2025-09-23 13:30:00	e5a6b7c8-9012-4d34-bcde-567890abcdef	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
61	2025-09-23 13:30:00	Learn SQL Basics in Databases	databases-1	SQL Basics	2025-09-23 13:30:00	f6b7c8d9-0123-4e45-cdef-67890abcdef1	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
62	2025-09-23 13:30:00	Learn PostgreSQL in Databases	databases-2	PostgreSQL	2025-09-23 13:30:00	f6b7c8d9-0123-4e45-cdef-67890abcdef1	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
63	2025-09-23 13:30:00	Learn MySQL in Databases	databases-3	MySQL	2025-09-23 13:30:00	f6b7c8d9-0123-4e45-cdef-67890abcdef1	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
64	2025-09-23 13:30:00	Learn MongoDB in Databases	databases-4	MongoDB	2025-09-23 13:30:00	f6b7c8d9-0123-4e45-cdef-67890abcdef1	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
65	2025-09-23 13:30:00	Learn Redis in Databases	databases-5	Redis	2025-09-23 13:30:00	f6b7c8d9-0123-4e45-cdef-67890abcdef1	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
66	2025-09-23 13:30:00	Learn UI Principles in Ui Ux	ui-ux-1	UI Principles	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
67	2025-09-23 13:30:00	Learn UX Research in Ui Ux	ui-ux-2	UX Research	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
68	2025-09-23 13:30:00	Learn Wireframing in Ui Ux	ui-ux-3	Wireframing	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
69	2025-09-23 13:30:00	Learn Prototyping in Ui Ux	ui-ux-4	Prototyping	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
70	2025-09-23 13:30:00	Learn Figma in Ui Ux	ui-ux-5	Figma	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
71	2025-09-23 13:30:00	Learn Adobe XD in Ui Ux	ui-ux-6	Adobe XD	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
72	2025-09-23 13:30:00	Learn Design Systems in Ui Ux	ui-ux-7	Design Systems	2025-09-23 13:30:00	07c8d9e0-1234-4f56-def0-7890abcdef12	\N	1a8f3943-acc7-42da-a007-83066bd39c52	\N	8748-884-DAG
\.


--
-- Data for Name: enrollment; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.enrollment (id, enrolled_at, course_id, user_id) FROM stdin;
ec56cec4-9004-4480-8e66-2cc61b6a19c9	2025-09-29 16:09:07.647053+05:30	1	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: exam; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.exam (id, created_at, ends_at, max_attempts, pass_marks, show_score_immediately, shuffle_questions, starts_at, status, time_limit_min, title, total_marks, updated_at, course_id, user_id) FROM stdin;
f09b21c4-ddb2-4a19-880a-66b5959de132	2025-09-27 12:33:59.77	2025-09-29 17:35:00+05:30	87	50	t	t	2025-09-29 17:30:00+05:30	3	6	Exam title is required	100	2025-09-27 12:33:59.77	9	1a8f3943-acc7-42da-a007-83066bd39c52
30dd7b85-5cca-4113-8b5f-3d90d71bc708	2025-09-27 12:59:32.841	2025-09-29 17:35:00+05:30	87	50	t	t	2025-09-29 17:30:00+05:30	3	6	Exam title is required	100	2025-09-27 12:59:32.841	9	1a8f3943-acc7-42da-a007-83066bd39c52
4ba0aca9-5e62-4905-8736-e809ce6f4f55	2025-09-29 10:44:55.655	2025-09-29 17:35:00+05:30	87	50	t	t	2025-09-29 17:30:00+05:30	0	6	Exam title is required	100	2025-09-29 10:44:55.655	1	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: exam_attempt; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.exam_attempt (id, created_at, is_attempt, is_completed, question_attempts, updated_at, exam_id, user_id) FROM stdin;
\.


--
-- Data for Name: lesson; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.lesson (id, created_at, description, status, thumbnail_url, "time", title, updated_at, video_url, course_id) FROM stdin;
4d9fe626-5df9-474c-918d-3b293c8fd07b	2025-10-07 11:50:03.915	Lesson Description is required	Lesson Status is required	Lesson Thumbnail URL is required	Lesson Duration is required	Lesson Title is required-9	2025-10-07 11:50:03.915	Lesson Video URL is required	1
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.payments (id, amount, created_at, currency, status, updated_at, course_id, pricing_plan_id, user_id) FROM stdin;
\.


--
-- Data for Name: pricing_plans; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.pricing_plans (id, currency, description, plan_type, price, title, course_id, created_at, updated_at) FROM stdin;
8748-884-DAG	Rupees	test desc	0	500	title	9	2025-09-01 12:54:35.367	2025-09-01 12:54:35.367
SDF-2	Ruppes	test monthly	1	10	monthly	9	2025-09-01 12:54:35.367	2025-09-01 12:54:35.367
SDF-3	Ruppes	test yearly	2	1000	yearly	9	2025-09-01 12:54:35.367	2025-09-01 12:54:35.367
SDF-4	Ruppes	test quartly	3	568	quartly	9	2025-09-01 12:54:35.367	2025-09-01 12:54:35.367
\.


--
-- Data for Name: question_attempt; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.question_attempt (id, answer, attempted_at, exam_id, question_id, user_id) FROM stdin;
\.


--
-- Data for Name: question_options; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.question_options (id, created_at, is_correct, option, updated_at, question_id) FROM stdin;
\.


--
-- Data for Name: questions; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.questions (id, created_at, description, marks, "position", title, type, updated_at, exam_id, question_options_id, correct_option) FROM stdin;
7cef5fea-ad7a-4628-96fe-a357e37fcc42	2025-09-27 12:59:51.633	Description is required -2	5.00	\N	Title is required	0	2025-09-27 12:59:51.633	30dd7b85-5cca-4113-8b5f-3d90d71bc708	\N	\N
df7f2049-03fa-4a58-acd9-d7720b084de3	2025-09-27 12:59:48.542	Description is required - updated	5.00	\N	Title is required	0	2025-09-27 13:11:27.677	30dd7b85-5cca-4113-8b5f-3d90d71bc708	\N	\N
\.


--
-- Data for Name: ratings; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.ratings (id, comment, created_at, rating, updated_at, course_id, user_id) FROM stdin;
e7e87b43-77bf-4c87-80de-90670f7fec0e	Test Comment	2025-10-28 15:09:53.146	4.0	2025-10-28 15:09:53.146	1	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: refresh_token; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.refresh_token (id, created_at, expires_at, token, user_id, client, ipaddress) FROM stdin;
22105ea3-433d-4c27-950e-c0e035435f9f	2025-10-29 13:00:21.865	2025-11-28 13:00:21.823	64ce29b5-9c59-4e72-8ee8-ee0caead8670	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
1733dc13-6f9a-4cd3-9255-eb4717e6b7ba	2025-12-22 16:33:35.642	2026-01-21 16:33:35.586	822cad22-6516-4783-90cc-f17f038f1888	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.51.0	0:0:0:0:0:0:0:1
0d1c4f23-2b5f-4daf-8ff8-dd9dfeb037e6	2025-10-29 10:08:54.261	2025-11-28 10:08:54.221	69fc08a7-d5f9-4366-9f00-b3522cb6e85b	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
f8dfebb3-5234-45d2-9905-016f5ba1a43f	2025-10-29 10:09:31.666	2025-11-28 10:09:31.634	f1b75955-fce0-4dc4-99bd-1d3547e7f671	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
5e0e81c8-7ecf-4ede-8cad-48102b0050c8	2025-10-29 10:09:55.685	2025-11-28 10:09:55.659	1f8a3e76-a0d1-470d-b048-30b88d5e025b	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
6c3420de-2e27-4823-9920-44daa9fca014	2025-10-29 10:16:09.694	2025-11-28 10:16:09.622	2a383e35-04d0-4efe-8a9a-f6283c9b5a50	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
7c9f366f-19c5-4fec-b715-717ef6c4189e	2025-10-29 10:16:50.716	2025-11-28 10:16:50.679	e79649c8-f09f-45a4-95a8-43e5967b8cce	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
382d04b4-403d-4d4e-a46a-3c38d6456a97	2025-10-29 10:17:07.931	2025-11-28 10:17:07.93	6d477d39-f238-4de4-a203-087d4fb65adf	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
90ce955f-ae48-42f2-b522-a7c944d4b5a9	2025-10-29 10:44:12.494	2025-11-28 10:44:12.447	309790bb-6f2c-4fd0-816d-49d676ef0aa1	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
ecebac5b-87aa-4c6a-b2c2-46e694d00644	2025-10-29 10:44:58.15	2025-11-28 10:44:58.094	d5061e3f-1183-4aa3-a1ab-56c29d2d1ed8	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
76ca4b9f-2531-4832-b44b-1c16027a2b1e	2025-10-29 10:45:09.135	2025-11-28 10:45:09.134	5e052b50-18b6-4af8-a118-c7f9eb2c8eaa	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
64375c87-9147-47da-a09b-75d3eb6edd41	2025-10-29 10:47:21.371	2025-11-28 10:47:21.345	d57dee62-39b1-46ad-bcfa-f1b684279cf9	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
97b9f1a2-4c13-4fc7-a58e-af1d81a63f18	2025-10-29 11:19:23.821	2025-11-28 11:19:23.765	5abdfcd9-c047-4439-ab24-144040708d3e	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
1641c427-4828-49f8-8d57-8da224afce2f	2025-10-29 11:19:26.255	2025-11-28 11:19:26.254	f32617af-2e18-4913-ad88-e4d335e1c179	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
47374d5d-42dc-4501-af71-70945a72553d	2025-10-29 11:19:27.822	2025-11-28 11:19:27.822	4050b78a-54e4-4a65-a5cf-9627795db51b	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
c1f41473-e958-4a97-91ca-1b968babd1dc	2025-10-29 11:20:49.844	2025-11-28 11:20:49.791	3730398a-a7ac-45f6-9bc4-7637b4f36a27	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
b2bebd83-681a-420e-9acd-5d85f63eeb0e	2025-10-29 11:31:45.01	2025-11-28 11:31:44.962	b1c04285-c267-476c-a99c-905baaf9cfc7	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
359d975b-eb44-4c2c-aead-378dac50d45b	2025-10-29 11:33:56.369	2025-11-28 11:33:56.309	e2282106-586b-48a8-adec-da9914984c22	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
74b14ac4-4173-4094-8773-93950a0de101	2025-10-29 11:34:28.77	2025-11-28 11:34:28.728	4b292e79-442b-41ff-bc7a-1dd29e9b5f66	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
f69fab5e-3c1d-4812-ace7-de4279ded47e	2025-10-29 11:35:15.479	2025-11-28 11:35:15.432	1e755dc1-afc1-4f29-b743-143dc081a707	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
fe764d90-9d19-4f65-901b-5fedf87c784c	2025-10-29 11:35:22.658	2025-11-28 11:35:22.658	9e35ae4d-d148-45b1-9d45-92a06b4fd465	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
6af13e8b-5f26-49d8-a3f5-ef8cfa73623f	2025-10-29 11:49:49.812	2025-11-28 11:49:49.767	f136540c-82b4-41fb-a1de-27968df5b4bb	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
59d1c258-9ca8-4d8d-af62-f04842f708a5	2025-10-29 11:54:14.661	2025-11-28 11:54:14.634	46f22dd5-a92c-48dc-aee7-aef58ffaa4dd	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
2f468b3d-c8a0-4c71-be47-d24aeb6ea6ab	2025-10-29 12:01:37.253	2025-11-28 12:01:37.214	fc80128f-fe9c-4bcb-9a91-5caf593d2c43	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
fc1ccaa6-d881-4656-a78c-e2ec1ea5eaae	2025-10-29 12:05:24.772	2025-11-28 12:05:24.705	a068717c-861e-4fbb-ab8f-c86634b407aa	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
2868b205-69f9-4da6-aa98-97e39d46af32	2025-10-29 12:37:22.084	2025-11-28 12:37:22.046	d6cd6330-a84e-4c11-8038-67bf7dd96300	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
c988b915-57ff-4aae-b78d-e7df279a3f31	2025-10-29 12:37:46.311	2025-11-28 12:37:46.31	d566effc-ea2c-4fb8-b257-df09582329da	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
6c76ec43-04c7-4c49-a1fc-7b852bdab2ed	2025-10-29 12:41:30.411	2025-11-28 12:41:30.38	980c0bbf-f7b0-4b49-92b0-8f3d85c3c980	1a8f3943-acc7-42da-a007-83066bd39c52	PostmanRuntime/7.49.0	0:0:0:0:0:0:0:1
\.


--
-- Data for Name: report_card; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.report_card (id, created_at, grade, obtained_marks, percentage, total_marks, exam_id, user_id) FROM stdin;
904ebe4b-7637-4afb-ad88-29325a7fe5f7	2025-09-29 16:09:12.745	F	50.00	50.00	100.00	4ba0aca9-5e62-4905-8736-e809ce6f4f55	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: review; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.review (id, created_at, updated_at, vote_type, course_id, user_id) FROM stdin;
SWFCRF	2025-09-02 16:45:37.562	2025-09-02 16:45:37.562	0	9	1a8f3943-acc7-42da-a007-83066bd39c52
SWFCRF2	2025-09-02 16:45:37.562	2025-09-02 16:45:37.562	0	9	1a8f3943-acc7-42da-a007-83066bd39c52
SWFCRF3.	2025-09-02 16:45:37.562	2025-09-02 16:45:37.562	1	9	1a8f3943-acc7-42da-a007-83066bd39c52
\.


--
-- Data for Name: tags; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.tags (id, name) FROM stdin;
547	arihant
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: arihant
--

COPY public.users (id, email, name, password, role, username, created_at, updated_at, is_active, is_banned, is_deleted, is_verified) FROM stdin;
1a8f3943-acc7-42da-a007-83066bd39c52	arihant@gmail.com	Arihant	$2a$12$8la86nFIoQJ40P3sR7/W7uvtqPsHv/tCABcmb95ZzJwvMv6GdG55u	ADMIN	arihant@125	2025-09-01 12:54:35.367	2025-09-01 12:54:35.367	t	f	f	t
a65129ff-097f-4f4d-8598-363b403bba96	testfrommobile@gmail.com	arihant	$2a$12$HFenh57Us86rd39obZ3Fgu3rcaYx/yVLMl.je0TEwz1Y51Ce0BduO	STUDENT	testfrommobile	2025-10-16 12:39:53.005	2025-10-16 12:39:53.005	t	f	f	t
2d4f88f4-c0d1-4755-b4b4-bdc1dfa7b789	arihantj81@gmail.com	arihant	$2a$12$m6ADasmGorK7T93uzNdp7.CHWyOFgNPUlWi3MD/u50xxA.faWOCuG	STUDENT	arihantj81@gmail.com	2025-10-27 16:52:54.589	2025-10-27 16:52:54.589	t	f	f	t
ac31c162-5650-4937-a9b6-d73063aae295	arihantjain916@gmail.com	Arihant	$2a$12$MNkrtORCQBTLTdlESuXjWesGWnpQNljA1TlYvZy3syYdDCAEdk5H.	ADMIN	arihantj916	2025-09-02 16:45:37.562	2025-09-02 16:45:37.562	t	t	t	t
d04cab45-1881-406b-a301-9ac628c0eff7	test@gmail.com	Test Jain	$2a$12$RZp.PzB79m.qu0EVGijvP.caJ7xZA3TqAvKD5SrqOP5Zck/2uIn2u	STUDENT	test@125	2025-09-29 12:27:38.892	2025-09-29 12:27:38.892	t	t	t	t
6bc0c171-490b-4237-acc8-1a76a65b93d0	arihant8@gmail.com	Arihant Jain	$2a$12$Soq16V0mCMCZuSAPwjkrYOED2BZssmlc30A1tVf1PZEiGKnqF1xhe	STUDENT	arihant@1256	2025-10-07 12:31:16.929	2025-10-07 12:31:16.929	t	f	f	f
\.


--
-- Name: course_views_seq; Type: SEQUENCE SET; Schema: public; Owner: arihant
--

SELECT pg_catalog.setval('public.course_views_seq', 601, true);


--
-- Name: courses_seq; Type: SEQUENCE SET; Schema: public; Owner: arihant
--

SELECT pg_catalog.setval('public.courses_seq', 351, true);


--
-- Name: ai_chat_bot ai_chat_bot_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.ai_chat_bot
    ADD CONSTRAINT ai_chat_bot_pkey PRIMARY KEY (id);


--
-- Name: ai_daily_usage ai_daily_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.ai_daily_usage
    ADD CONSTRAINT ai_daily_usage_pkey PRIMARY KEY (id);


--
-- Name: asset_meta asset_meta_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.asset_meta
    ADD CONSTRAINT asset_meta_pkey PRIMARY KEY (id);


--
-- Name: asset asset_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.asset
    ADD CONSTRAINT asset_pkey PRIMARY KEY (id);


--
-- Name: blog_comment blog_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog_comment
    ADD CONSTRAINT blog_comment_pkey PRIMARY KEY (id);


--
-- Name: blog blog_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT blog_pkey PRIMARY KEY (id);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: contact_us contact_us_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.contact_us
    ADD CONSTRAINT contact_us_pkey PRIMARY KEY (id);


--
-- Name: course_views course_views_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.course_views
    ADD CONSTRAINT course_views_pkey PRIMARY KEY (id);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);


--
-- Name: enrollment enrollment_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_pkey PRIMARY KEY (id);


--
-- Name: exam_attempt exam_attempt_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.exam_attempt
    ADD CONSTRAINT exam_attempt_pkey PRIMARY KEY (id);


--
-- Name: exam exam_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.exam
    ADD CONSTRAINT exam_pkey PRIMARY KEY (id);


--
-- Name: lesson lesson_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.lesson
    ADD CONSTRAINT lesson_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: pricing_plans pricing_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.pricing_plans
    ADD CONSTRAINT pricing_plans_pkey PRIMARY KEY (id);


--
-- Name: question_attempt question_attempt_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.question_attempt
    ADD CONSTRAINT question_attempt_pkey PRIMARY KEY (id);


--
-- Name: question_options question_options_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.question_options
    ADD CONSTRAINT question_options_pkey PRIMARY KEY (id);


--
-- Name: questions questions_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_pkey PRIMARY KEY (id);


--
-- Name: ratings ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_pkey PRIMARY KEY (id);


--
-- Name: refresh_token refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (id);


--
-- Name: report_card report_card_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_pkey PRIMARY KEY (id);


--
-- Name: review review_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.review
    ADD CONSTRAINT review_pkey PRIMARY KEY (id);


--
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: enrollment uk1o10lxhcpndqk31apt7jtpuip; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT uk1o10lxhcpndqk31apt7jtpuip UNIQUE (course_id, user_id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: course_views uk_course_user; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.course_views
    ADD CONSTRAINT uk_course_user UNIQUE (course_id, user_id);


--
-- Name: category ukhqknmjh5423vchi4xkyhxlhg2; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT ukhqknmjh5423vchi4xkyhxlhg2 UNIQUE (slug);


--
-- Name: asset_meta ukimh315u4h2na2igcg8lltqudl; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.asset_meta
    ADD CONSTRAINT ukimh315u4h2na2igcg8lltqudl UNIQUE (asset_id);


--
-- Name: ai_daily_usage ukl50rrhayg08dd017vidrlpmn; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.ai_daily_usage
    ADD CONSTRAINT ukl50rrhayg08dd017vidrlpmn UNIQUE (user_id, usage_date);


--
-- Name: users ukr43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: blog ukrnrou1m94mucgt39epcw8ov59; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT ukrnrou1m94mucgt39epcw8ov59 UNIQUE (title);


--
-- Name: blog ukrse7kjvydwev63jjbsm0dw4ey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT ukrse7kjvydwev63jjbsm0dw4ey UNIQUE (slug);


--
-- Name: tags ukt48xdq560gs3gap9g7jg36kgc; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT ukt48xdq560gs3gap9g7jg36kgc UNIQUE (name);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_course_views_course_time; Type: INDEX; Schema: public; Owner: arihant
--

CREATE INDEX idx_course_views_course_time ON public.course_views USING btree (course_id, created_at);


--
-- Name: idx_course_views_created_at; Type: INDEX; Schema: public; Owner: arihant
--

CREATE INDEX idx_course_views_created_at ON public.course_views USING btree (created_at);


--
-- Name: questions fk2b4fa7kl7w7798hu403hg2twi; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk2b4fa7kl7w7798hu403hg2twi FOREIGN KEY (exam_id) REFERENCES public.exam(id);


--
-- Name: blog_comment fk3tlf8ln9y3n59pdaqrq88lf0h; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog_comment
    ADD CONSTRAINT fk3tlf8ln9y3n59pdaqrq88lf0h FOREIGN KEY (parent_id) REFERENCES public.blog_comment(id);


--
-- Name: question_attempt fk4kjpch7kjeduk17b4qmaxm6v6; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.question_attempt
    ADD CONSTRAINT fk4kjpch7kjeduk17b4qmaxm6v6 FOREIGN KEY (exam_id) REFERENCES public.exam(id);


--
-- Name: enrollment fk4x08no2mpupkr616h50w3aksx; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT fk4x08no2mpupkr616h50w3aksx FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: courses fk51k53m6m5gi9n91fnlxkxgpmv; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fk51k53m6m5gi9n91fnlxkxgpmv FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: review fk6cpw2nlklblpvc7hyt7ko6v3e; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.review
    ADD CONSTRAINT fk6cpw2nlklblpvc7hyt7ko6v3e FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: enrollment fk7ofybdo2o0ngc4de3uvx4dxqv; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT fk7ofybdo2o0ngc4de3uvx4dxqv FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: payments fk8nlm4urshp5drsk0nlkprig36; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fk8nlm4urshp5drsk0nlkprig36 FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: exam_attempt fka5mo4rt8qbna94i1m7sjrfnv; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.exam_attempt
    ADD CONSTRAINT fka5mo4rt8qbna94i1m7sjrfnv FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: review fka76dd0r1lr1k0cuah145lib3r; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.review
    ADD CONSTRAINT fka76dd0r1lr1k0cuah145lib3r FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: ratings fkb3354ee2xxvdrbyq9f42jdayd; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT fkb3354ee2xxvdrbyq9f42jdayd FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: blog_comment fkb9cpog8ie2cyapsyyt7gikpbl; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog_comment
    ADD CONSTRAINT fkb9cpog8ie2cyapsyyt7gikpbl FOREIGN KEY (blog_id) REFERENCES public.blog(id);


--
-- Name: ratings fkbegfifgmkbhd1pj5vitred35g; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT fkbegfifgmkbhd1pj5vitred35g FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: lesson fkbnmu2xq9klrrg69rs7lcn5fku; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.lesson
    ADD CONSTRAINT fkbnmu2xq9klrrg69rs7lcn5fku FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: courses fkcyfum8goa6q5u13uog0563gyp; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fkcyfum8goa6q5u13uog0563gyp FOREIGN KEY (instructor_id) REFERENCES public.users(id);


--
-- Name: exam fkemywihfkga7ba286m04l0fwdo; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.exam
    ADD CONSTRAINT fkemywihfkga7ba286m04l0fwdo FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: pricing_plans fkf6xksib5dtoob27w6b7t286ka; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.pricing_plans
    ADD CONSTRAINT fkf6xksib5dtoob27w6b7t286ka FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: asset_meta fkf7qx64ondupmle1sfoskgdcnj; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.asset_meta
    ADD CONSTRAINT fkf7qx64ondupmle1sfoskgdcnj FOREIGN KEY (asset_id) REFERENCES public.asset(id);


--
-- Name: asset fkiqobt75euu1len9rfn4d6dn6j; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.asset
    ADD CONSTRAINT fkiqobt75euu1len9rfn4d6dn6j FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: payments fkj94hgy9v5fw1munb90tar2eje; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkj94hgy9v5fw1munb90tar2eje FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: refresh_token fkjtx87i0jvq2svedphegvdwcuy; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fkjtx87i0jvq2svedphegvdwcuy FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: question_attempt fkk2il6ptmy4nt6yhuq5mhryeil; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.question_attempt
    ADD CONSTRAINT fkk2il6ptmy4nt6yhuq5mhryeil FOREIGN KEY (question_id) REFERENCES public.questions(id);


--
-- Name: payments fkk69h81au1lpx1i8kiph0yhk4l; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkk69h81au1lpx1i8kiph0yhk4l FOREIGN KEY (pricing_plan_id) REFERENCES public.pricing_plans(id);


--
-- Name: questions fkkf9hbmc4jghal4n1s3ax88n7m; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fkkf9hbmc4jghal4n1s3ax88n7m FOREIGN KEY (question_options_id) REFERENCES public.question_options(id);


--
-- Name: blog fkkr2fy24puc3x3sdnla4r1iok1; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog
    ADD CONSTRAINT fkkr2fy24puc3x3sdnla4r1iok1 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: courses fklov3acbar51wk6de8posn7ucc; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fklov3acbar51wk6de8posn7ucc FOREIGN KEY (pricing_plan_id) REFERENCES public.pricing_plans(id);


--
-- Name: report_card fkmvy0c9e9dcj5ifmmmfd6vvs9a; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT fkmvy0c9e9dcj5ifmmmfd6vvs9a FOREIGN KEY (exam_id) REFERENCES public.exam(id);


--
-- Name: exam_attempt fkn1sj3wwcaqpmn5t43fukvnpwv; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.exam_attempt
    ADD CONSTRAINT fkn1sj3wwcaqpmn5t43fukvnpwv FOREIGN KEY (exam_id) REFERENCES public.exam(id);


--
-- Name: exam fkp2cn3ovuuk5nq51hsltwpgl42; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.exam
    ADD CONSTRAINT fkp2cn3ovuuk5nq51hsltwpgl42 FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: courses fkqbllephdg8tp1tltr3opj0y0; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fkqbllephdg8tp1tltr3opj0y0 FOREIGN KEY (category_id) REFERENCES public.category(id);


--
-- Name: report_card fkqcqxfm1m62smrrvu1wtu8l2x0; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT fkqcqxfm1m62smrrvu1wtu8l2x0 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: course_views fkr5isby8qr32qn1eslutugfur9; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.course_views
    ADD CONSTRAINT fkr5isby8qr32qn1eslutugfur9 FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: blog_comment fkrd59qyd7xdutfpjl05212w526; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.blog_comment
    ADD CONSTRAINT fkrd59qyd7xdutfpjl05212w526 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: question_attempt fkrdocll5oy5vlicbqsh2vc56jk; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.question_attempt
    ADD CONSTRAINT fkrdocll5oy5vlicbqsh2vc56jk FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: question_options fksb9v00wdrgc9qojtjkv7e1gkp; Type: FK CONSTRAINT; Schema: public; Owner: arihant
--

ALTER TABLE ONLY public.question_options
    ADD CONSTRAINT fksb9v00wdrgc9qojtjkv7e1gkp FOREIGN KEY (question_id) REFERENCES public.questions(id);


--
-- PostgreSQL database dump complete
--

