    create table matches (
        is_bye boolean not null,
        round_number integer not null,
        created_at timestamp(6) not null,
        black_player_id uuid,
        id uuid not null,
        tournament_id uuid not null,
        white_player_id uuid,
        result enum ('BLACK_WIN','DRAW','UNPLAYED','WHITE_WIN') not null,
        primary key (id)
    );

    create table players (
        rating integer not null,
        created_at timestamp(6) not null,
        id uuid not null,
        name varchar(255) not null,
        primary key (id)
    );

    create table tournament_players (
        color_difference integer not null,
        score numeric(3,1) not null,
        player_id uuid not null,
        tournament_id uuid not null,
        primary key (player_id, tournament_id)
    );

    create table tournaments (
        current_round integer not null,
        total_rounds integer not null,
        share_code varchar(6) unique,
        created_at timestamp(6) not null,
        id uuid not null,
        name varchar(255) not null,
        status enum ('DRAFT','FINISHED','IN_PROGRESS') not null,
        primary key (id)
    );

    alter table if exists matches
       add constraint FK6lpqh8jloqjq14lep76ye6i1q
       foreign key (black_player_id)
       references players;

    alter table if exists matches
       add constraint FKeeniokyjgo5k6rmhjujatn27i
       foreign key (tournament_id)
       references tournaments;

    alter table if exists matches
       add constraint FKn4u94md77541pxqdji151shbp
       foreign key (white_player_id)
       references players;

    alter table if exists tournament_players
       add constraint FKmeedwyshf0r8brn8awbfhmsa3
       foreign key (player_id)
       references players;

    alter table if exists tournament_players
       add constraint FK5wly4mkm6bgcge42bkdnt1ujg
       foreign key (tournament_id)
       references tournaments;
