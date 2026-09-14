use cron::Schedule;
use std::str::FromStr;

#[test]
fn test_cron_parsing() {
    let exprs = vec![
        "0 0 16 * * MON-FRI *",
        "0 0 16 * * 1-5 *",
        "0 0 9 * * * *",
        "0 0 * * * * *",
    ];
    for e in exprs {
        let parsed = Schedule::from_str(e);
        println!("{}: {:?}", e, parsed.is_ok());
        assert!(parsed.is_ok());
    }
}
