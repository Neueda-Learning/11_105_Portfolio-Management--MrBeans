export const ANALOGIES = [
  { value: 5,         emoji: '☕',  name: 'Fancy Coffee',      desc: 'A premium barista pour-over' },
  { value: 15,        emoji: '🎬',  name: 'Movie Night',       desc: 'A cinema ticket + popcorn' },
  { value: 30,        emoji: '⚾',  name: 'MLB Baseball',      desc: 'An official league baseball' },
  { value: 60,        emoji: '🎮',  name: 'AAA Video Game',    desc: 'A brand-new premium game' },
  { value: 120,       emoji: '👟',  name: 'Nike Sneakers',     desc: 'A pair of Air Max sneakers' },
  { value: 250,       emoji: '🎤',  name: 'Concert Ticket',    desc: 'Front row at a live show' },
  { value: 600,       emoji: '🍽️', name: 'Michelin Dinner',   desc: 'Fine dining for two' },
  { value: 1_000,     emoji: '✈️',  name: 'Weekend Getaway',   desc: 'A flight + hotel for two nights' },
  { value: 1_500,     emoji: '📱',  name: 'iPhone 16 Pro',     desc: 'Latest Apple flagship phone' },
  { value: 2_500,     emoji: '💻',  name: 'MacBook Pro',       desc: 'A high-performance laptop' },
  { value: 5_000,     emoji: '🏖️', name: 'Europe Vacation',   desc: '7-day trip through Europe' },
  { value: 10_000,    emoji: '💎',  name: 'Diamond Ring',      desc: 'A 0.5ct diamond engagement ring' },
  { value: 15_000,    emoji: '🛵',  name: 'Motorcycle',        desc: 'A mid-range street bike' },
  { value: 25_000,    emoji: '🚗',  name: 'New Sedan',         desc: 'A brand-new Toyota Camry' },
  { value: 55_000,    emoji: '🏎️', name: 'Sports Car',        desc: 'A Chevrolet Corvette' },
  { value: 85_000,    emoji: '🚙',  name: 'Luxury SUV',        desc: 'A BMW X5 or Mercedes GLE' },
  { value: 100_000,   emoji: '⛵',  name: 'Sailing Yacht',     desc: 'A 25-foot cruising yacht' },
  { value: 200_000,   emoji: '🏠',  name: 'House',             desc: 'Down payment on a family home' },
  { value: 500_000,   emoji: '🏡',  name: 'Luxury Villa',      desc: 'A luxury property overseas' },
  { value: 1_000_000, emoji: '🏝️', name: 'Private Island',    desc: 'Your own Caribbean island' },
];

/** Format dollar value as $5 / $1K / $1.5M */
export const fmtAnalogy = (n) =>
  n >= 1_000_000
    ? `$${(n / 1_000_000).toFixed(n % 1_000_000 === 0 ? 0 : 1)}M`
    : n >= 1_000
    ? `$${Math.round(n / 1_000)}K`
    : `$${n}`;
