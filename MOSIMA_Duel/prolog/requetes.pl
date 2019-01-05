use_module(library(jpl)).

shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */
see(EnemyInSight, Class):-
	not(EnemyInSight),
	jpl_call(Class, evaluateBestPos, [], @(void)).

toOpenFire(EnemyInSight, P):-
	EnemyInSight,
	shotImpact(P).

explore(Class):-
	jpl_call(Class, executeExplore, [], @(void)).

attack(EnemyInSight, Class):-
	EnemyInSight,
	jpl_call(Class, executeAttack, [], @(void)).